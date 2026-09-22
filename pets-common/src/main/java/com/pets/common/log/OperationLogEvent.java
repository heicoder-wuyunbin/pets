package com.pets.common.log;

/**
 * 操作日志事件。
 * LogAspect 采集信息后发布此事件，由具体服务（如 system-service）监听并落库。
 */
public class OperationLogEvent {

    private Long shopId;
    private Long userId;
    private String userName;
    private String module;
    private String action;
    private String params;
    private String ip;
    private Integer status;
    private String failReason;

    public OperationLogEvent() {}

    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getParams() { return params; }
    public void setParams(String params) { this.params = params; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }
}
