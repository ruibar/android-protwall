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

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

// This requires list view items with equal heights

public class ExpandedListView extends ListView {
    public ExpandedListView(Context context) {
        super(context);
    }

    public ExpandedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandedListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 4, MeasureSpec.AT_MOST));
    }
}