package com.netsensia.rivalchess.config;

import com.netsensia.rivalchess.uci.UCIController;

public enum BuildInfo {

    VERSION ("2020.03"),
    BUILD (UCIController.class.getPackage().getImplementationVersion()),
    ;

    private String value;

    private BuildInfo(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
