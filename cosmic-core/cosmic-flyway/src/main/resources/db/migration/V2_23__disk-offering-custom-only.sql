UPDATE `disk_offering` SET `disk_size`=0, `customized`=1, `state`="Inactive", `removed`=NOW() WHERE `type`="disk" AND `customized` = 0;
