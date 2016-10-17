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

public class Packet {
    public long time;
    public int version;
    public int protocol;
    public String flags;
    public String saddr;
    public int sport;
    public String daddr;
    public int dport;
    public String data;
    public int uid;
    public boolean allowed;

    public Packet() {
    }

    @Override
    public String toString() {
        return "uid=" + uid + " v" + version + " p" + protocol + " " + daddr + "/" + dport;
    }
}
