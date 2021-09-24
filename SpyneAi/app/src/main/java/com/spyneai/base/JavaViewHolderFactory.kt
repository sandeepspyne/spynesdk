package com.spyneai.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.OngoingTestHolder
import com.spyneai.R
import com.spyneai.shoot.holders.SubcategoryHolder


object JavaViewHolderFactory {

    fun create(view: View, viewType: Int, listener: OnItemClickListener): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_ongoing_test -> OngoingTestHolder(view, listener)
            R.layout.item_subcategories -> SubcategoryHolder(view, listener)
//            R.layout.item_home_grid -> TopicViewHolder(view, listener)
//            R.layout.item_home_row_topic -> HomeRowTopicHolder(view, listener)
//            R.layout.item_home_row_response -> HomeRowResponseHolder(view, listener)
//            R.layout.item_discover_urfeed -> DiscoverURFeedViewHolder(view, listener)
//            R.layout.item_discover_category -> CategoryHolder(view, listener)
//            R.layout.item_home_row_thread -> HomeRowTheadHolder(view, listener)
//            R.layout.item_home_thread_new -> HomeTheadHolder(view, listener)
//            R.layout.item_discover_people -> DiscoverPeopleHolder(view, listener)
//            R.layout.item_discover_feed -> DiscoverFeedHolder(view, listener)
//            R.layout.item_recommended_feed -> DiscoverFeedHolder(view, listener)
//            R.layout.item_respond -> RespondButtonHolder(view, listener)
//            R.layout.item_feed_member -> FeedMemberHolder(view, listener)
//            R.layout.item_discussion -> DiscussionHolder(view, listener)
//            R.layout.item_blocked_user -> UserViewHolder(view, listener)
            else -> GenericViewHolder(view)
        }
    }



}