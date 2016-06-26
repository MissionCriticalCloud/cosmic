package com.cloud.storage.dao;

import com.cloud.storage.LaunchPermissionVO;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.storage.VMTemplateVO;
import com.cloud.utils.DateUtil;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LaunchPermissionDaoImpl extends GenericDaoBase<LaunchPermissionVO, Long> implements LaunchPermissionDao {
    private static final Logger s_logger = LoggerFactory.getLogger(LaunchPermissionDaoImpl.class);
    private static final String REMOVE_LAUNCH_PERMISSION = "DELETE FROM `cloud`.`launch_permission`" + "  WHERE template_id = ? AND account_id = ?";

    private static final String LIST_PERMITTED_TEMPLATES =
            "SELECT t.id, t.unique_name, t.name, t.public, t.format, t.type, t.hvm, t.bits, t.url, t.created, t.account_id, t.checksum, t.display_text, t.enable_password, t" +
                    ".guest_os_id, t.featured"
                    + "  FROM `cloud`.`vm_template` t INNER JOIN (SELECT lp.template_id as lptid"
                    + " FROM `cloud`.`launch_permission` lp"
                    + " WHERE lp.account_id = ?) joinlp"
                    + "  WHERE t.id = joinlp.lptid" + "  ORDER BY t.created DESC";

    private final SearchBuilder<LaunchPermissionVO> TemplateAndAccountSearch;
    private final SearchBuilder<LaunchPermissionVO> TemplateIdSearch;

    protected LaunchPermissionDaoImpl() {
        TemplateAndAccountSearch = createSearchBuilder();
        TemplateAndAccountSearch.and("templateId", TemplateAndAccountSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        TemplateAndAccountSearch.and("accountId", TemplateAndAccountSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        TemplateAndAccountSearch.done();

        TemplateIdSearch = createSearchBuilder();
        TemplateIdSearch.and("templateId", TemplateIdSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        TemplateIdSearch.done();
    }

    @Override
    public void removePermissions(final long templateId, final List<Long> accountIds) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        try {
            txn.start();
            final String sql = REMOVE_LAUNCH_PERMISSION;
            pstmt = txn.prepareAutoCloseStatement(sql);
            for (final Long accountId : accountIds) {
                pstmt.setLong(1, templateId);
                pstmt.setLong(2, accountId.longValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            txn.commit();
        } catch (final Exception e) {
            txn.rollback();
            s_logger.warn("Error removing launch permissions", e);
            throw new CloudRuntimeException("Error removing launch permissions", e);
        }
    }

    @Override
    public void removeAllPermissions(final long templateId) {
        final SearchCriteria<LaunchPermissionVO> sc = TemplateIdSearch.create();
        sc.setParameters("templateId", templateId);
        expunge(sc);
    }

    @Override
    public LaunchPermissionVO findByTemplateAndAccount(final long templateId, final long accountId) {
        final SearchCriteria<LaunchPermissionVO> sc = TemplateAndAccountSearch.create();
        sc.setParameters("templateId", templateId);
        sc.setParameters("accountId", accountId);
        return findOneBy(sc);
    }

    @Override
    public List<LaunchPermissionVO> findByTemplate(final long templateId) {
        final SearchCriteria<LaunchPermissionVO> sc = TemplateIdSearch.create();
        sc.setParameters("templateId", templateId);
        return listBy(sc);
    }

    @Override
    public List<VMTemplateVO> listPermittedTemplates(final long accountId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        final List<VMTemplateVO> permittedTemplates = new ArrayList<>();
        PreparedStatement pstmt = null;
        try {
            final String sql = LIST_PERMITTED_TEMPLATES;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, accountId);
            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                final long id = rs.getLong(1);
                final String uniqueName = rs.getString(2);
                final String name = rs.getString(3);
                final boolean isPublic = rs.getBoolean(4);
                final String value = rs.getString(5);
                final ImageFormat format = ImageFormat.valueOf(value);
                final String tmpltType = rs.getString(6);
                final boolean requiresHVM = rs.getBoolean(7);
                final int bits = rs.getInt(8);
                final String url = rs.getString(9);
                final String createdTS = rs.getString(10);
                final long templateAccountId = rs.getLong(11);
                final String checksum = rs.getString(12);
                final String displayText = rs.getString(13);
                final boolean enablePassword = rs.getBoolean(14);
                final long guestOSId = rs.getLong(15);
                final boolean featured = rs.getBoolean(16);
                Date createdDate = null;

                if (createdTS != null) {
                    createdDate = DateUtil.parseDateString(s_gmtTimeZone, createdTS);
                }

                if (isPublic) {
                    continue; // if it's public already, skip adding it to
                    // permitted templates as this for private
                    // templates only
                }
                final VMTemplateVO template =
                        new VMTemplateVO(id, uniqueName, name, format, isPublic, featured, TemplateType.valueOf(tmpltType), url, createdDate, requiresHVM, bits,
                                templateAccountId, checksum, displayText, enablePassword, guestOSId, true, null);
                permittedTemplates.add(template);
            }
        } catch (final Exception e) {
            s_logger.warn("Error listing permitted templates", e);
        }
        return permittedTemplates;
    }
}
