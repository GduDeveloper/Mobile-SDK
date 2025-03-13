package com.gdu.demo.flight.setting.bean

data class GetAiModelResponse(
    val count: Int,
    val models: List<GetAiModel>
)