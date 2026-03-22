import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { Member } from '@/types';

interface AuthState {
  user: Member | null;
  token: string | null;
  isLoading: boolean;
  login: (user: Member, token: string) => void;
  logout: () => void;
  setLoading: (loading: boolean) => void;
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isLoading: false,

      login: (user: Member, token: string) => {
        set({ user, token });
        // localStorage에도 저장 (axios 인터셉터에서 사용)
        if (typeof window !== 'undefined') {
          localStorage.setItem('accessToken', token);
          localStorage.setItem('user', JSON.stringify(user));
        }
      },

      logout: () => {
        set({ user: null, token: null });
        // localStorage에서도 제거
        if (typeof window !== 'undefined') {
          localStorage.removeItem('accessToken');
          localStorage.removeItem('user');
        }
      },

      setLoading: (isLoading: boolean) => set({ isLoading }),

      isAuthenticated: () => {
        const { user, token } = get();
        return user !== null && token !== null;
      },
    }),
    {
      name: 'auth-storage', // localStorage 키 이름
      partialize: (state) => ({
        user: state.user,
        token: state.token,
      }),
    }
  )
);