import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { ShopOption } from '@/types/api';

interface UserInfo {
  shopId: number | null;
  shopName: string | null;
  roleCode: string | null;
  roleName: string | null;
}

interface AuthState {
  token: string | null;
  user: UserInfo | null;
  /** 多岗位场景下，登录后需选店时暂存的预授权信息 */
  preAuthToken: string | null;
  pendingShops: ShopOption[] | null;
  login: (token: string, user: UserInfo) => void;
  setPreAuth: (token: string, shops: ShopOption[]) => void;
  clearPreAuth: () => void;
  setToken: (token: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      user: null,
      preAuthToken: null,
      pendingShops: null,
      login: (token, user) => set({ token, user, preAuthToken: null, pendingShops: null }),
      setPreAuth: (token, shops) => set({ preAuthToken: token, pendingShops: shops }),
      clearPreAuth: () => set({ preAuthToken: null, pendingShops: null }),
      setToken: (token) => set({ token }),
      logout: () => set({ token: null, user: null, preAuthToken: null, pendingShops: null }),
    }),
    { name: 'pets-auth' },
  ),
);
