package com.protoolapps.firewall;

/*
    This file is part of ProtWall.

    ProtWall is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ProtWall is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ProtWall.  If not, see <http://www.gnu.org/licenses/>.


*/

public class Version implements Comparable<Version> {

    private String version;

    public Version(String version) {
        this.version = version.replace("-beta", "");
    }

    @Override
    public int compareTo(Version other) {
        String[] lhs = this.version.split("\\.");
        String[] rhs = other.version.split("\\.");
        int length = Math.max(lhs.length, rhs.length);
        for (int i = 0; i < length; i++) {
            int vLhs = (i < lhs.length ? Integer.parseInt(lhs[i]) : 0);
            int vRhs = (i < rhs.length ? Integer.parseInt(rhs[i]) : 0);
            if (vLhs < vRhs)
                return -1;
            if (vLhs > vRhs)
                return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return version;
    }
}
