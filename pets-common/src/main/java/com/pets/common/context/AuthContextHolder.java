package com.pets.common.context;

public final class AuthContextHolder {
    private static final ThreadLocal<AuthContext> HOLDER = new ThreadLocal<>();

    private AuthContextHolder() {}

    public static void set(AuthContext ctx) { HOLDER.set(ctx); }
    public static AuthContext get() { return HOLDER.get(); }
    public static void clear() { HOLDER.remove(); }

    public static class AuthContext {
        private Long accountId;
        private Long clerkId;
        private Long shopId;
        private Long chainGroupId;
        private Long roleId;

        public Long getAccountId() { return accountId; }
        public void setAccountId(Long accountId) { this.accountId = accountId; }

        public Long getClerkId() { return clerkId; }
        public void setClerkId(Long clerkId) { this.clerkId = clerkId; }

        public Long getShopId() { return shopId; }
        public void setShopId(Long shopId) { this.shopId = shopId; }

        public Long getChainGroupId() { return chainGroupId; }
        public void setChainGroupId(Long chainGroupId) { this.chainGroupId = chainGroupId; }

        public Long getRoleId() { return roleId; }
        public void setRoleId(Long roleId) { this.roleId = roleId; }
    }
}
