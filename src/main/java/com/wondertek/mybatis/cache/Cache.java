package com.wondertek.mybatis.cache;

import com.wondertek.mybatis.annotation.VersionLocker;

import java.util.Arrays;
import java.util.Objects;

public interface Cache<T> {
    boolean containMethodSignature(VersionLockerCache.MethodSignature vm);

    void cacheMethod(VersionLockerCache.MethodSignature vm, VersionLocker versionLocker);

    T getVersionLocker(VersionLockerCache.MethodSignature vm);

    class MethodSignature {
        private String id;
        private Class<?>[] params;

        public MethodSignature(String id, Class<?>[] params) {
            this.id = id;
            this.params = params;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Class<?>[] getParams() {
            return params;
        }

        public void setParams(Class<?>[] params) {
            this.params = params;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodSignature that = (MethodSignature) o;
            return Objects.equals(id, that.id) &&
                    Arrays.equals(params, that.params);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(id);
            result = 31 * result + Arrays.hashCode(params);
            return result;
        }
    }
}
