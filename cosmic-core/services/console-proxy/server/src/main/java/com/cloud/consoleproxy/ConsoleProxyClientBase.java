package com.cloud.consoleproxy;

import com.cloud.consoleproxy.util.TileInfo;
import com.cloud.consoleproxy.util.TileTracker;
import com.cloud.consoleproxy.vnc.FrameBufferCanvas;

import java.awt.Image;
import java.awt.Rectangle;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * an instance of specialized console protocol implementation, such as VNC
 * <p>
 * It mainly implements the features needed by front-end AJAX viewer
 */
public abstract class ConsoleProxyClientBase implements ConsoleProxyClient, ConsoleProxyClientListener {
    private static final Logger s_logger = Logger.getLogger(ConsoleProxyClientBase.class);

    private static int s_nextClientId = 0;
    protected int clientId = getNextClientId();

    protected long ajaxSessionId = 0;

    protected boolean dirtyFlag = false;
    protected Object tileDirtyEvent = new Object();
    protected TileTracker tracker;
    protected AjaxFIFOImageCache ajaxImageCache = new AjaxFIFOImageCache(2);

    protected ConsoleProxyClientParam clientParam;
    protected String clientToken;

    protected long createTime = System.currentTimeMillis();
    protected long lastFrontEndActivityTime = System.currentTimeMillis();

    protected boolean framebufferResized = false;
    protected int resizedFramebufferWidth;
    protected int resizedFramebufferHeight;

    public ConsoleProxyClientBase() {
        tracker = new TileTracker();
        tracker.initTracking(64, 64, 800, 600);
    }

    //
    // Helpers
    //
    private synchronized static int getNextClientId() {
        return ++s_nextClientId;
    }

    //
    // interface ConsoleProxyClient
    //
    @Override
    public int getClientId() {
        return clientId;
    }

    @Override
    public abstract boolean isHostConnected();

    @Override
    public abstract boolean isFrontEndAlive();

    @Override
    public long getAjaxSessionId() {
        return this.ajaxSessionId;
    }

    @Override
    public AjaxFIFOImageCache getAjaxImageCache() {
        return ajaxImageCache;
    }

    @Override
    public Image getClientScaledImage(final int width, final int height) {
        final FrameBufferCanvas canvas = getFrameBufferCavas();
        if (canvas != null) {
            return canvas.getFrameBufferScaledImage(width, height);
        }

        return null;
    }

    @Override
    public String onAjaxClientStart(final String title, final List<String> languages, final String guest) {
        updateFrontEndActivityTime();

        if (!waitForViewerReady()) {
            return onAjaxClientConnectFailed();
        }

        synchronized (this) {
            ajaxSessionId++;
            framebufferResized = false;
        }

        final int tileWidth = tracker.getTileWidth();
        final int tileHeight = tracker.getTileHeight();
        final int width = tracker.getTrackWidth();
        final int height = tracker.getTrackHeight();

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Ajax client start, frame buffer w: " + width + ", " + height);
        }

        final List<TileInfo> tiles = tracker.scan(true);
        final String imgUrl = prepareAjaxImage(tiles, true);
        final String updateUrl = prepareAjaxSession(true);

        final StringBuffer sbTileSequence = new StringBuffer();
        int i = 0;
        for (final TileInfo tile : tiles) {
            sbTileSequence.append("[").append(tile.getRow()).append(",").append(tile.getCol()).append("]");
            if (i < tiles.size() - 1) {
                sbTileSequence.append(",");
            }

            i++;
        }

        return getAjaxViewerPageContent(sbTileSequence.toString(), imgUrl, updateUrl, width, height, tileWidth, tileHeight, title,
                ConsoleProxy.keyboardType == ConsoleProxy.KEYBOARD_RAW, languages, guest, this.clientParam.getLocale());
    }

    @Override
    public String onAjaxClientUpdate() {
        updateFrontEndActivityTime();
        if (!waitForViewerReady()) {
            return onAjaxClientDisconnected();
        }

        synchronized (tileDirtyEvent) {
            if (!dirtyFlag) {
                try {
                    tileDirtyEvent.wait(3000);
                } catch (final InterruptedException e) {
                    s_logger.debug("[ignored] Console proxy ajax update was interupted while waiting for viewer to become ready.");
                }
            }
        }

        boolean doResize = false;
        synchronized (this) {
            if (framebufferResized) {
                framebufferResized = false;
                doResize = true;
            }
        }

        final List<TileInfo> tiles;

        if (doResize) {
            tiles = tracker.scan(true);
        } else {
            tiles = tracker.scan(false);
        }
        dirtyFlag = false;

        final String imgUrl = prepareAjaxImage(tiles, false);
        final StringBuffer sbTileSequence = new StringBuffer();
        int i = 0;
        for (final TileInfo tile : tiles) {
            sbTileSequence.append("[").append(tile.getRow()).append(",").append(tile.getCol()).append("]");
            if (i < tiles.size() - 1) {
                sbTileSequence.append(",");
            }

            i++;
        }

        return getAjaxViewerUpdatePageContent(sbTileSequence.toString(), imgUrl, doResize, resizedFramebufferWidth, resizedFramebufferHeight, tracker.getTileWidth(),
                tracker.getTileHeight());
    }

    @Override
    public String onAjaxClientKickoff() {
        return "onKickoff();";
    }

    @Override
    public abstract void sendClientRawKeyboardEvent(InputEventType event, int code, int modifiers);

    @Override
    public abstract void sendClientMouseEvent(InputEventType event, int x, int y, int code, int modifiers);

    @Override
    public long getClientCreateTime() {
        return createTime;
    }

    @Override
    public long getClientLastFrontEndActivityTime() {
        return lastFrontEndActivityTime;
    }

    @Override
    public String getClientHostAddress() {
        return clientParam.getClientHostAddress();
    }

    @Override
    public int getClientHostPort() {
        return clientParam.getClientHostPort();
    }

    @Override
    public String getClientHostPassword() {
        return clientParam.getClientHostPassword();
    }

    @Override
    public String getClientTag() {
        if (clientParam.getClientTag() != null) {
            return clientParam.getClientTag();
        }
        return "";
    }

    @Override
    public abstract void initClient(ConsoleProxyClientParam param);

    @Override
    public abstract void closeClient();

    public String onAjaxClientDisconnected() {
        return "onDisconnect();";
    }

    private String getAjaxViewerUpdatePageContent(final String tileSequence, final String imgUrl, final boolean resized, final int width, final int height, final int tileWidth,
                                                  final int tileHeight) {

        final String[] content =
                new String[]{"tileMap = [ " + tileSequence + " ];",
                        resized ? "ajaxViewer.resize('main_panel', " + width + ", " + height + " , " + tileWidth + ", " + tileHeight + ");" : "",
                        "ajaxViewer.refresh('" + imgUrl + "', tileMap, false);"};

        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < content.length; i++) {
            sb.append(content[i]);
        }

        return sb.toString();
    }

    public void updateFrontEndActivityTime() {
        lastFrontEndActivityTime = System.currentTimeMillis();
    }

    private boolean waitForViewerReady() {
        final long startTick = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTick < 5000) {
            if (getFrameBufferCavas() != null) {
                return true;
            }

            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                s_logger.debug("[ignored] Console proxy was interupted while waiting for viewer to become ready.");
            }
        }
        return false;
    }

    private String onAjaxClientConnectFailed() {
        return "<html><head></head><body><div id=\"main_panel\" tabindex=\"1\"><p>"
                + "Unable to start console session as connection is refused by the machine you are accessing" + "</p></div></body></html>";
    }

    private String prepareAjaxImage(final List<TileInfo> tiles, final boolean init) {
        final byte[] imgBits;
        if (init) {
            imgBits = getFrameBufferJpeg();
        } else {
            imgBits = getTilesMergedJpeg(tiles, tracker.getTileWidth(), tracker.getTileHeight());
        }

        if (imgBits == null) {
            s_logger.warn("Unable to generate jpeg image");
        } else {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Generated jpeg image size: " + imgBits.length);
            }
        }

        final int key = ajaxImageCache.putImage(imgBits);
        final StringBuffer sb = new StringBuffer();
        sb.append("/ajaximg?token=").append(clientToken);
        sb.append("&key=").append(key);
        sb.append("&ts=").append(System.currentTimeMillis());

        return sb.toString();
    }

    private String prepareAjaxSession(final boolean init) {
        if (init) {
            synchronized (this) {
                ajaxSessionId++;
            }
        }

        final StringBuffer sb = new StringBuffer();
        sb.append("/ajax?token=").append(clientToken).append("&sess=").append(ajaxSessionId);
        return sb.toString();
    }

    private String getAjaxViewerPageContent(final String tileSequence, final String imgUrl, final String updateUrl, final int width, final int height, final int tileWidth, final
    int tileHeight, final String title,
                                            final boolean rawKeyboard, final List<String> languages, final String guest, final String locale) {

        final StringBuffer sbLanguages = new StringBuffer("");
        if (languages != null) {
            for (final String lang : languages) {
                if (sbLanguages.length() > 0) {
                    sbLanguages.append(",");
                }
                sbLanguages.append(lang);
            }
        }

        final String[] content =
                new String[]{"<html>", "<head>", "<script type=\"text/javascript\" language=\"javascript\" src=\"/resource/js/jquery.js\"></script>",
                        "<script type=\"text/javascript\" language=\"javascript\" src=\"/resource/js/cloud.logger.js\"></script>",
                        "<script type=\"text/javascript\" language=\"javascript\" src=\"/resource/js/ajaxkeys.js\"></script>",
                        "<script type=\"text/javascript\" language=\"javascript\" src=\"/resource/js/ajaxviewer.js\"></script>",
                        "<script type=\"text/javascript\" language=\"javascript\" src=\"/resource/js/handler.js\"></script>",
                        "<link rel=\"stylesheet\" type=\"text/css\" href=\"/resource/css/ajaxviewer.css\"></link>",
                        "<link rel=\"stylesheet\" type=\"text/css\" href=\"/resource/css/logger.css\"></link>", "<title>" + title + "</title>", "</head>", "<body>",
                        "<div id=\"toolbar\">", "<ul>", "<li>", "<a href=\"#\" cmd=\"sendCtrlAltDel\">",
                        "<span><img align=\"left\" src=\"/resource/images/cad.gif\" alt=\"Ctrl-Alt-Del\" />Ctrl-Alt-Del</span>", "</a>", "</li>", "<li>",
                        "<a href=\"#\" cmd=\"sendCtrlEsc\">",
                        "<span><img align=\"left\" src=\"/resource/images/winlog.png\" alt=\"Ctrl-Esc\" style=\"width:16px;height:16px\"/>Ctrl-Esc</span>", "</a>", "</li>",

                        "<li class=\"pulldown\">", "<a href=\"#\">",
                        "<span><img align=\"left\" src=\"/resource/images/winlog.png\" alt=\"Keyboard\" style=\"width:16px;height:16px\"/>Keyboard</span>", "</a>", "<ul>",
                        "<li><a href=\"#\" cmd=\"keyboard_us\"><span>Standard (US) keyboard</span></a></li>",
                        "<li><a href=\"#\" cmd=\"keyboard_uk\"><span>UK keyboard</span></a></li>",
                        "<li><a href=\"#\" cmd=\"keyboard_jp\"><span>Japanese keyboard</span></a></li>",
                        "<li><a href=\"#\" cmd=\"keyboard_fr\"><span>French AZERTY keyboard</span></a></li>", "</ul>", "</li>", "</ul>",
                        "<span id=\"light\" class=\"dark\" cmd=\"toggle_logwin\"></span>", "</div>", "<div id=\"main_panel\" tabindex=\"1\"></div>",
                        "<script language=\"javascript\">", "var acceptLanguages = '" + sbLanguages.toString() + "';", "var tileMap = [ " + tileSequence + " ];",
                        "var ajaxViewer = new AjaxViewer('main_panel', '" + imgUrl + "', '" + updateUrl + "', '" + locale + "', '" + guest + "', tileMap, ",
                        String.valueOf(width) + ", " + String.valueOf(height) + ", " + String.valueOf(tileWidth) + ", " + String.valueOf(tileHeight) + ");",

                        "$(function() {", "ajaxViewer.start();", "});",

                        "</script>", "</body>", "</html>"};

        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < content.length; i++) {
            sb.append(content[i]);
        }

        return sb.toString();
    }

    //
    // AJAX Image manipulation
    //
    public byte[] getFrameBufferJpeg() {
        final FrameBufferCanvas canvas = getFrameBufferCavas();
        if (canvas != null) {
            return canvas.getFrameBufferJpeg();
        }

        return null;
    }

    public byte[] getTilesMergedJpeg(final List<TileInfo> tileList, final int tileWidth, final int tileHeight) {
        final FrameBufferCanvas canvas = getFrameBufferCavas();
        if (canvas != null) {
            return canvas.getTilesMergedJpeg(tileList, tileWidth, tileHeight);
        }
        return null;
    }

    protected abstract FrameBufferCanvas getFrameBufferCavas();

    //
    // interface FrameBufferEventListener
    //
    @Override
    public void onFramebufferSizeChange(final int w, final int h) {
        tracker.resize(w, h);

        synchronized (this) {
            framebufferResized = true;
            resizedFramebufferWidth = w;
            resizedFramebufferHeight = h;
        }

        signalTileDirtyEvent();
    }

    @Override
    public void onFramebufferUpdate(final int x, final int y, final int w, final int h) {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Frame buffer update {" + x + "," + y + "," + w + "," + h + "}");
        }
        tracker.invalidate(new Rectangle(x, y, w, h));

        signalTileDirtyEvent();
    }

    private void signalTileDirtyEvent() {
        synchronized (tileDirtyEvent) {
            dirtyFlag = true;
            tileDirtyEvent.notifyAll();
        }
    }

    public ConsoleProxyClientParam getClientParam() {
        return clientParam;
    }

    public void setClientParam(final ConsoleProxyClientParam clientParam) {
        this.clientParam = clientParam;
        final ConsoleProxyPasswordBasedEncryptor encryptor = new ConsoleProxyPasswordBasedEncryptor(ConsoleProxy.getEncryptorPassword());
        this.clientToken = encryptor.encryptObject(ConsoleProxyClientParam.class, clientParam);
    }
}
