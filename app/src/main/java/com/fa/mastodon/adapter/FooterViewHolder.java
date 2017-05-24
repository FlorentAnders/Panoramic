/* Copyright 2017 Andrew Dawson
 *
 * This file is a part of Tusky.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tusky is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tusky; if not,
 * see <http://www.gnu.org/licenses>. */

package com.fa.mastodon.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.keylesspalace.tusky.R;

class FooterViewHolder extends RecyclerView.ViewHolder {
    FooterViewHolder(View itemView) {
        super(itemView);
        ProgressBar progressBar = (ProgressBar) itemView.findViewById(R.id.footer_progress_bar);
        if (progressBar != null) {
            progressBar.setIndeterminate(true);
        }
    }
}