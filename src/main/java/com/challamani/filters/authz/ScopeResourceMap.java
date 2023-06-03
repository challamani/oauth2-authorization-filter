package com.challamani.filters.authz;

import lombok.Data;

import java.util.List;

@Data
public class ScopeResourceMap {
    private String resourceId;
    private List<String> methods;
    private List<String> scopes;
}
