package com.example.licentaremastered.scotty

object ScootyConst {
    const val POINT_PROXIMITY = 0.0035
    const val INTERSECT_PROXIMITY = 0.00013
    const val BUFFERING_MIN_DISTANCE = 0.0001
    const val FREE_TRAVEL = 1
    const val DESTINATION = 2
    const val LOCATION_PERMISSION_REQUEST_CODE = 1
    val locationDebugList: List<Pair<Double, Double>> = listOf(
        Pair(44.179918025053595, 28.628213911987533),
        Pair(44.17997957892059, 28.628015428523582),
        Pair(44.18001035582999, 28.62785449598525),
        Pair(44.18005267405417, 28.627623826013632)
    )
}