package com.cloud.template;

import com.cloud.api.ApiConstants;
import com.cloud.api.ApiDBUtils;
import com.cloud.api.command.user.iso.DeleteIsoCmd;
import com.cloud.api.command.user.iso.RegisterIsoCmd;
import com.cloud.api.command.user.template.DeleteTemplateCmd;
import com.cloud.api.command.user.template.ExtractTemplateCmd;
import com.cloud.api.command.user.template.GetUploadParamsForTemplateCmd;
import com.cloud.api.command.user.template.RegisterTemplateCmd;
import com.cloud.configuration.Config;
import com.cloud.context.CallContext;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.domain.dao.DomainDao;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.host.dao.HostDao;
import com.cloud.legacymodel.configuration.Resource.ResourceType;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.PermissionDeniedException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.storage.TemplateType;
import com.cloud.legacymodel.storage.VirtualMachineTemplate;
import com.cloud.legacymodel.user.Account;
import com.cloud.model.enumeration.AllocationState;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.model.enumeration.MaintenancePolicy;
import com.cloud.model.enumeration.OptimiseFor;
import com.cloud.projects.ProjectManager;
import com.cloud.storage.GuestOS;
import com.cloud.storage.TemplateProfile;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.datastore.db.ImageStoreDao;
import com.cloud.storage.datastore.db.ImageStoreVO;
import com.cloud.storage.datastore.db.TemplateDataStoreDao;
import com.cloud.user.AccountManager;
import com.cloud.user.ResourceLimitService;
import com.cloud.user.UserVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;
import com.cloud.utils.EnumUtils;
import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.dao.UserVmDao;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TemplateAdapterBase extends AdapterBase implements TemplateAdapter {
    private final static Logger s_logger = LoggerFactory.getLogger(TemplateAdapterBase.class);
    protected
    @Inject
    DomainDao _domainDao;
    protected
    @Inject
    AccountDao _accountDao;
    protected
    @Inject
    ConfigurationDao _configDao;
    protected
    @Inject
    UserDao _userDao;
    protected
    @Inject
    AccountManager _accountMgr;
    protected
    @Inject
    DataCenterDao _dcDao;
    protected
    @Inject
    VMTemplateDao _tmpltDao;
    protected
    @Inject
    TemplateDataStoreDao _tmpltStoreDao;
    protected
    @Inject
    HostDao _hostDao;
    protected
    @Inject
    UserVmDao _userVmDao;
    protected
    @Inject
    ResourceLimitService _resourceLimitMgr;
    protected
    @Inject
    ImageStoreDao _imgStoreDao;
    @Inject
    ProjectManager _projectMgr;

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public TemplateProfile prepare(final RegisterTemplateCmd cmd) throws ResourceAllocationException {
        //check if the caller can operate with the template owner
        final Account caller = CallContext.current().getCallingAccount();
        final Account owner = _accountMgr.getAccount(cmd.getEntityOwnerId());
        _accountMgr.checkAccess(caller, null, true, owner);

        final boolean isRouting = (cmd.isRoutingType() == null) ? false : cmd.isRoutingType();

        Long zoneId = cmd.getZoneId();
        // ignore passed zoneId if we are using region wide image store
        final List<ImageStoreVO> stores = _imgStoreDao.findRegionImageStores();
        if (stores != null && stores.size() > 0) {
            zoneId = -1L;
        }

        return prepare(false, CallContext.current().getCallingUserId(), cmd.getTemplateName(), cmd.getDisplayText(), cmd.getBits(), cmd.isPasswordEnabled(),
                cmd.getUrl(), cmd.isPublic(), cmd.isFeatured(), cmd.isExtractable(), cmd.getFormat(), cmd.getOsTypeId(), zoneId,
                HypervisorType.getType(cmd.getHypervisor()), cmd.getChecksum(), true, cmd.getTemplateTag(), owner, cmd.getDetails(), cmd.isSshKeyEnabled(), null,
                cmd.isDynamicallyScalable(), isRouting ? TemplateType.ROUTING : TemplateType.USER, cmd.getManufacturerString(), cmd.getOptimiseFor(), cmd.getMaintenancePolicy(),
                cmd.getIsRemoteGatewayTemplate());
    }

    @Override
    public TemplateProfile prepare(final GetUploadParamsForTemplateCmd cmd) throws ResourceAllocationException {
        //check if the caller can operate with the template owner
        final Account caller = CallContext.current().getCallingAccount();
        final Account owner = _accountMgr.getAccount(cmd.getEntityOwnerId());
        _accountMgr.checkAccess(caller, null, true, owner);

        final boolean isRouting = (cmd.isRoutingType() == null) ? false : cmd.isRoutingType();

        Long zoneId = cmd.getZoneId();
        // ignore passed zoneId if we are using region wide image store
        final List<ImageStoreVO> stores = _imgStoreDao.findRegionImageStores();
        if (stores != null && stores.size() > 0) {
            zoneId = -1L;
        }

        return prepare(false, CallContext.current().getCallingUserId(), cmd.getName(), cmd.getDisplayText(), cmd.getBits(), cmd.isPasswordEnabled(),
                null, cmd.isPublic(), cmd.isFeatured(), cmd.isExtractable(), cmd.getFormat(), cmd.getOsTypeId(), zoneId,
                HypervisorType.getType(cmd.getHypervisor()), cmd.getChecksum(), true, cmd.getTemplateTag(), owner, cmd.getDetails(), cmd.isSshKeyEnabled(), null,
                cmd.isDynamicallyScalable(), isRouting ? TemplateType.ROUTING : TemplateType.USER, "Mission Critical Cloud", OptimiseFor.Generic, MaintenancePolicy.LiveMigrate, false);
    }

    @Override
    public TemplateProfile prepare(final RegisterIsoCmd cmd) throws ResourceAllocationException {
        //check if the caller can operate with the template owner
        final Account caller = CallContext.current().getCallingAccount();
        final Account owner = _accountMgr.getAccount(cmd.getEntityOwnerId());
        _accountMgr.checkAccess(caller, null, true, owner);

        Long zoneId = cmd.getZoneId();
        // ignore passed zoneId if we are using region wide image store
        final List<ImageStoreVO> stores = _imgStoreDao.findRegionImageStores();
        if (stores != null && stores.size() > 0) {
            zoneId = -1L;
        }

        return prepare(true, CallContext.current().getCallingUserId(), cmd.getIsoName(), cmd.getDisplayText(), 64, false, cmd.getUrl(), cmd.isPublic(),
                cmd.isFeatured(), cmd.isExtractable(), ImageFormat.ISO.toString(), cmd.getOsTypeId(), zoneId, HypervisorType.None, cmd.getChecksum(), cmd.isBootable(), null,
                owner, null, false, cmd.getImageStoreUuid(), cmd.isDynamicallyScalable(), TemplateType.USER, cmd.getManufacturerString(), cmd.getOptimiseFor(),
                cmd.getMaintenancePolicy(), false) ;
    }

    @Override
    abstract public VMTemplateVO create(TemplateProfile profile);

    @Override
    public TemplateProfile prepareDelete(final DeleteTemplateCmd cmd) {
        final Long templateId = cmd.getId();
        Long userId = CallContext.current().getCallingUserId();
        final Account account = CallContext.current().getCallingAccount();
        final Long zoneId = cmd.getZoneId();

        final VMTemplateVO template = _tmpltDao.findById(templateId);
        if (template == null) {
            throw new InvalidParameterValueException("unable to find template with id " + templateId);
        }

        userId = accountAndUserValidation(account, userId, null, template, "Unable to delete template ");

        final UserVO user = _userDao.findById(userId);
        if (user == null) {
            throw new InvalidParameterValueException("Please specify a valid user.");
        }

        if (template.getFormat() == ImageFormat.ISO) {
            throw new InvalidParameterValueException("Please specify a valid template.");
        }

        return new TemplateProfile(userId, template, zoneId);
    }

    private Long accountAndUserValidation(final Account account, final long userId, final UserVmVO vmInstanceCheck, final VMTemplateVO template, final String msg) throws
            PermissionDeniedException {

        if (account != null) {
            if (!_accountMgr.isAdmin(account.getId())) {
                if ((vmInstanceCheck != null) && (account.getId() != vmInstanceCheck.getAccountId())) {
                    throw new PermissionDeniedException(msg + ". Permission denied.");
                }

                if ((template != null) &&
                        (!template.isPublicTemplate() && (account.getId() != template.getAccountId()) && (template.getTemplateType() != TemplateType.PERHOST))) {
                    //special handling for the project case
                    final Account owner = _accountMgr.getAccount(template.getAccountId());
                    if (owner.getType() == Account.ACCOUNT_TYPE_PROJECT) {
                        if (!_projectMgr.canAccessProjectAccount(account, owner.getId())) {
                            throw new PermissionDeniedException(msg + ". Permission denied. The caller can't access project's template");
                        }
                    } else {
                        throw new PermissionDeniedException(msg + ". Permission denied.");
                    }
                }
            } else {
                if ((vmInstanceCheck != null) && !_domainDao.isChildDomain(account.getDomainId(), vmInstanceCheck.getDomainId())) {
                    throw new PermissionDeniedException(msg + ". Permission denied.");
                }
                // FIXME: if template/ISO owner is null we probably need to
                // throw some kind of exception

                if (template != null) {
                    final Account templateOwner = _accountDao.findById(template.getAccountId());
                    if ((templateOwner != null) && !_domainDao.isChildDomain(account.getDomainId(), templateOwner.getDomainId())) {
                        throw new PermissionDeniedException(msg + ". Permission denied.");
                    }
                }
            }
        }

        return userId;
    }

    @Override
    public TemplateProfile prepareDelete(final DeleteIsoCmd cmd) {
        final Long templateId = cmd.getId();
        Long userId = CallContext.current().getCallingUserId();
        final Account account = CallContext.current().getCallingAccount();
        final Long zoneId = cmd.getZoneId();

        final VMTemplateVO template = _tmpltDao.findById(templateId);
        if (template == null) {
            throw new InvalidParameterValueException("unable to find iso with id " + templateId);
        }

        userId = accountAndUserValidation(account, userId, null, template, "Unable to delete iso ");

        final UserVO user = _userDao.findById(userId);
        if (user == null) {
            throw new InvalidParameterValueException("Please specify a valid user.");
        }

        if (template.getFormat() != ImageFormat.ISO) {
            throw new InvalidParameterValueException("Please specify a valid iso.");
        }

        return new TemplateProfile(userId, template, zoneId);
    }

    @Override
    public TemplateProfile prepareExtractTemplate(final ExtractTemplateCmd cmd) {
        final Long templateId = cmd.getId();
        final Long userId = CallContext.current().getCallingUserId();
        final Long zoneId = cmd.getZoneId();

        final VMTemplateVO template = _tmpltDao.findById(templateId);
        if (template == null) {
            throw new InvalidParameterValueException("unable to find template with id " + templateId);
        }
        return new TemplateProfile(userId, template, zoneId);
    }

    @Override
    abstract public boolean delete(TemplateProfile profile);

    @Override
    public TemplateProfile prepare(final boolean isIso, final Long userId, final String name, final String displayText, final Integer bits, final Boolean passwordEnabled,
                                   final String url, final Boolean isPublic, final Boolean featured, final Boolean isExtractable, final String format, final Long guestOSId, final Long zoneId,
                                   final HypervisorType hypervisorType, final String accountName,
                                   final Long domainId, final String chksum, final Boolean bootable, final Map details) throws ResourceAllocationException {
        return prepare(isIso, userId, name, displayText, bits, passwordEnabled, url, isPublic, featured, isExtractable, format, guestOSId, zoneId,
                hypervisorType, chksum, bootable, null, null, details, false, null, false, TemplateType.USER, "Mission Critical Cloud", OptimiseFor.Generic, MaintenancePolicy.LiveMigrate, false);
    }

    @Override
    public TemplateProfile prepare(final boolean isIso, final long userId, final String name, final String displayText, Integer bits, Boolean passwordEnabled, final String url,
                                   Boolean isPublic, Boolean featured, Boolean isExtractable, final String format, Long guestOSId, Long zoneId, final HypervisorType hypervisorType,
                                   final String chksum, Boolean bootable, final String templateTag, final Account templateOwner, Map details, Boolean sshkeyEnabled,
                                   final String imageStoreUuid, final Boolean isDynamicallyScalable, final TemplateType templateType, final String manufacturerString,
                                   final OptimiseFor optimiseFor, final MaintenancePolicy maintenancePolicy, Boolean isRemoteGatewayTemplate) throws ResourceAllocationException {
        //Long accountId = null;
        // parameters verification

        if (isPublic == null) {
            isPublic = Boolean.FALSE;
        }

        if (zoneId.longValue() == -1) {
            zoneId = null;
        }

        if (isIso) {
            if (bootable == null) {
                bootable = Boolean.TRUE;
            }
            final GuestOS noneGuestOs = ApiDBUtils.findGuestOSByDisplayName(ApiConstants.ISO_GUEST_OS_NONE);
            if ((guestOSId == null || noneGuestOs == null || guestOSId == noneGuestOs.getId()) && bootable == true) {
                throw new InvalidParameterValueException("Please pass a valid GuestOS Id");
            }
            if (bootable == false) {
                guestOSId = noneGuestOs.getId(); //Guest os id of None.
            }
        } else {
            if (bits == null) {
                bits = Integer.valueOf(64);
            }
            if (passwordEnabled == null) {
                passwordEnabled = false;
            }
        }

        if (isExtractable == null) {
            isExtractable = Boolean.FALSE;
        }
        if (sshkeyEnabled == null) {
            sshkeyEnabled = Boolean.FALSE;
        }
        if (isRemoteGatewayTemplate == null) {
            isRemoteGatewayTemplate = Boolean.FALSE;
        }

        final boolean isAdmin = _accountMgr.isRootAdmin(templateOwner.getId());
        boolean isRegionStore = false;
        final List<ImageStoreVO> stores = _imgStoreDao.findRegionImageStores();
        if (stores != null && stores.size() > 0) {
            isRegionStore = true;
        }

        if (!isAdmin && zoneId == null && !isRegionStore) {
            // domain admin and user should also be able to register template on a region store
            throw new InvalidParameterValueException("Please specify a valid zone Id. Only admins can create templates in all zones.");
        }

        // check for the url format only when url is not null. url can be null incase of form based upload
        if (url != null && url.toLowerCase().contains("file://")) {
            throw new InvalidParameterValueException("File:// type urls are currently unsupported");
        }

        // check whether owner can create public templates
        final boolean allowPublicUserTemplates = TemplateManager.AllowPublicUserTemplates.valueIn(templateOwner.getId());
        if (!isAdmin && !allowPublicUserTemplates && isPublic) {
            throw new InvalidParameterValueException("Only private templates/ISO can be created.");
        }

        if (!isAdmin || featured == null) {
            featured = Boolean.FALSE;
        }

        final ImageFormat imgfmt;
        try {
            imgfmt = ImageFormat.valueOf(format.toUpperCase());
        } catch (final IllegalArgumentException e) {
            s_logger.debug("ImageFormat IllegalArgumentException: " + e.getMessage());
            throw new IllegalArgumentException("Image format: " + format + " is incorrect. Supported formats are " + EnumUtils.listValues(ImageFormat.values()));
        }

        // Check that the resource limit for templates/ISOs won't be exceeded
        final UserVO user = _userDao.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Unable to find user with id " + userId);
        }

        _resourceLimitMgr.checkResourceLimit(templateOwner, ResourceType.template);

        // If a zoneId is specified, make sure it is valid
        if (zoneId != null) {
            final DataCenterVO zone = _dcDao.findById(zoneId);
            if (zone == null) {
                throw new IllegalArgumentException("Please specify a valid zone.");
            }
            final Account caller = CallContext.current().getCallingAccount();
            if (AllocationState.Disabled == zone.getAllocationState() && !_accountMgr.isRootAdmin(caller.getId())) {
                throw new PermissionDeniedException("Cannot perform this operation, Zone is currently disabled: " + zoneId);
            }
        }

        final List<VMTemplateVO> systemvmTmplts = _tmpltDao.listAllSystemVMTemplates();
        for (final VMTemplateVO template : systemvmTmplts) {
            if (template.getName().equalsIgnoreCase(name) || template.getDisplayText().equalsIgnoreCase(displayText)) {
                throw new IllegalArgumentException("Cannot use reserved names for templates");
            }
        }

        if (hypervisorType.equals(HypervisorType.XenServer)) {
            if (details == null || !details.containsKey("hypervisortoolsversion") || details.get("hypervisortoolsversion") == null ||
                    ((String) details.get("hypervisortoolsversion")).equalsIgnoreCase("none")) {
                final String hpvs = _configDao.getValue(Config.XenServerPVdriverVersion.key());
                if (hpvs != null) {
                    if (details == null) {
                        details = new HashMap<String, String>();
                    }
                    details.put("hypervisortoolsversion", hpvs);
                }
            }
        }

        // When template contains Win, it should be set to OptimiseFor Windows
        OptimiseFor correctedOptimiseFor = optimiseFor;
        if (name != null && name.toLowerCase().contains("win") && optimiseFor != OptimiseFor.Windows) {
            s_logger.debug("Template name '" + name + "' contains 'win' so setting OptimiseFor to Windows");
            correctedOptimiseFor = OptimiseFor.Windows;
        }

        final Long id = _tmpltDao.getNextInSequence(Long.class, "id");
        CallContext.current().setEventDetails("Id: " + id + " name: " + name);
        return new TemplateProfile(id, userId, name, displayText, bits, passwordEnabled, url, isPublic, featured, isExtractable, imgfmt, guestOSId, zoneId,
                hypervisorType, templateOwner.getAccountName(), templateOwner.getDomainId(), templateOwner.getAccountId(), chksum, bootable, templateTag, details,
                sshkeyEnabled, null, isDynamicallyScalable, templateType, manufacturerString, correctedOptimiseFor, maintenancePolicy, isRemoteGatewayTemplate);
    }

    protected VMTemplateVO persistTemplate(final TemplateProfile profile, final VirtualMachineTemplate.State initialState) {
        final Long zoneId = profile.getZoneId();
        final VMTemplateVO template =
                new VMTemplateVO(profile.getTemplateId(), profile.getName(), profile.getFormat(), profile.getIsPublic(), profile.getFeatured(), profile.getIsExtractable(),
                        profile.getTemplateType(), profile.getUrl(), profile.getBits(), profile.getAccountId(), profile.getCheckSum(),
                        profile.getDisplayText(), profile.getPasswordEnabled(), profile.getGuestOsId(), profile.getBootable(), profile.getHypervisorType(),
                        profile.getTemplateTag(), profile.getDetails(), profile.getSshKeyEnabled(), profile.IsDynamicallyScalable());
        template.setState(initialState);
        template.setMaintenancePolicy(profile.getMaintenancePolicy());
        template.setManufacturerString(profile.getManufacturerString());
        template.setOptimiseFor(profile.getOptimiseFor());
        template.setMacLearning(false);
        template.setRemoteGatewayTemplate(profile.getRemoteGatewayTemplate());

        if (zoneId == null || zoneId.longValue() == -1) {
            final List<DataCenterVO> dcs = _dcDao.listAll();

            if (dcs.isEmpty()) {
                throw new CloudRuntimeException("No zones are present in the system, can't add template");
            }

            template.setCrossZones(true);
            for (final DataCenterVO dc : dcs) {
                _tmpltDao.addTemplateToZone(template, dc.getId());
            }
        } else {
            _tmpltDao.addTemplateToZone(template, zoneId);
        }
        return _tmpltDao.findById(template.getId());
    }
}
