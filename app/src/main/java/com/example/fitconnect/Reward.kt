package com.example.fitconnect

data class Reward(
    var rewardId: String? = null,
    var title: String? = null,
    var description: String? = null,
    var totalCount: Int? = null,
    var currentCount: Int = 0,
    var earned: Boolean = false
)
