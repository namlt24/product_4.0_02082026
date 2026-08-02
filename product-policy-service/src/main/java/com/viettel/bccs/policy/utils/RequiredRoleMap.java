package com.viettel.bccs.policy.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequiredRoleMap implements Serializable {

    @Builder.Default
    private List<String> values = new ArrayList<>();

    public boolean hasRole(String key) {
        return values != null && values.contains(key);
    }

    public boolean hasListRole(List<String> keys, String defaultKey) {
        if (keys == null || keys.isEmpty()) {
            return values != null && values.contains(defaultKey);
        }
        if (values == null) {
            return false;
        }
        for (String key : keys) {
            if (values.contains(key)) {
                return true;
            }
        }
        return false;
    }
}