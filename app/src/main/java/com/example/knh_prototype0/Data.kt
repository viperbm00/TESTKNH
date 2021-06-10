package com.example.knh_prototype0

//영양성분
data class NutritionFacts(var fid : Int, var fname : String, var carb : Double, var protein : Double, var fat : Double, var pergram : Double, var kcal: Double)

//사용자의 영양성분 기록
data class NutritionFactsRecord(var recordtime : String, var nutritionFacts : NutritionFacts, var intake : Int)

//운동정보
data class Exercise(var eid : Int, var ename : String, var MET : Double)

//사용자의 운동정보 기록
data class ExerciseRecord(var recordtime : String, var exercise: Exercise, var weight : Int, var etime : Int, var totalKcal : Int)