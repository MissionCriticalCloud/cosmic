package com.cloud.api.query.vo;

public abstract class BaseViewVO {

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (getId() ^ (getId() >>> 32));
        return result;
    }

    public abstract long getId();

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BaseViewVO other = (BaseViewVO) obj;
        if (getId() != other.getId()) {
            return false;
        }
        return true;
    }
}
