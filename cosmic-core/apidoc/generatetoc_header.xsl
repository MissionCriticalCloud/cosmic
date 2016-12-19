<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
    <xsl:output method="html" doctype-public="-//W3C//DTD HTML 1.0 Transitional//EN" />
    <xsl:template match="/">
        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                <link rel="stylesheet" href="includes/main.css" type="text/css" />
                <link rel="shortcut icon" href="favicon.ico" type="image/x-icon" />

                <title>Cosmic API Reference</title>
            </head>

            <body>
                <div id="insidetopbg">
                    <div id="inside_wrapper">
                        <div class="uppermenu_panel">
                            <div class="uppermenu_box"></div>
                        </div>

                        <div id="main_master">
                            <div id="inside_header">
                                <div class="header_top">
                                    <a class="cloud_logo" href="https://github.com/MissionCriticalCloud/cosmic/issues"></a>
                                    <div class="mainemenu_panel">

                                    </div>
                                </div>

                            </div>
                            <div id="main_content">

                                <div class="inside_apileftpanel">
                                    <div class="inside_contentpanel" style="width:930px;">
                                        <h1>API Documentation</h1>
                                        <a class="api_backbutton" href="javascript:window.history.back();"></a>
                                        <div class="apiannouncement_box">
                                            <div class="apiannouncement_contentarea">
                                            </div>
                                        </div>

                                        <div class="api_leftsections">
                                            <h3>%API_HEADER%</h3>
                                            <span>Commands available through the developer API URL and the integration API URL.</span>
                                            <div class="api_legends">
                                                <p>
                                                    <span class="api_legends_async">(A)</span>
                                                    implies that the command is asynchronous.
                                                </p>
                                                <p>(*) implies element has a child.</p>
                                            </div>
