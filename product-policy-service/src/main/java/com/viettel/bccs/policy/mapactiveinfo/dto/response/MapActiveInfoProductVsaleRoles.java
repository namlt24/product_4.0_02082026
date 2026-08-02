package com.viettel.bccs.policy.mapactiveinfo.dto.response;

import com.viettel.bccs.policy.client.dto.OptionSetValueResponse;

import java.util.List;

public record MapActiveInfoProductVsaleRoles(
        OptionSetValueResponse onVsaleRole,
        List<String> roleM2Ms,
        List<String> roleDBs,
        List<String> roleGOITHUONGs
) {}