package com.example.knh_prototype0

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.time.LocalDate

//식단 관련 DB
class NutritionFactsDBHelper(val context:Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION)
{
    companion object{
        val DB_NAME = "NutritionFacts.db"
        val DB_VERSION = 1
        val TABLE_NAME_NFRECORD = "nutritionfactsRecord"
        val TABLE_NAME_NUTRITIONFACTS = "nutritionfacts"
        //기록한 시간
        val RECORDTIME = "recordtime"
        //운동 id
        val FID = "eid"
        //운동 이름
        val FNAME = "ename"
        //탄수화물
        val CARB = "carb"
        //단백질
        val PROTEIN = "protein"
        //지방
        val FAT = "fat"
        //1회 제공량
        val GRAM = "gram"
        //1회 제공량 당 칼로리
        val KCAL = "kcal"
        //섭취량
        val INTAKE = "intake"
    }

    override fun onCreate(db: SQLiteDatabase?)
    {
        Log.i("shinee", "oncreate")

        val create_table_NF = "create table if not exists $TABLE_NAME_NUTRITIONFACTS("+
                "$FID integer primary key autoincrement, "+
                "$FNAME text, "+
                "$CARB integer, "+
                "$PROTEIN integer, "+
                "$FAT integer, "+
                "$GRAM integer, "+
                "$KCAL integer);"

        val create_table_NFR = "create table if not exists $TABLE_NAME_NFRECORD("+
                "$RECORDTIME text primary key, "+
                "$FID integer references $TABLE_NAME_NUTRITIONFACTS($FID) on update cascade, "+
                "$INTAKE integer);"

        db!!.execSQL(create_table_NF)
        db!!.execSQL(create_table_NFR)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int)
    {
        val drop_Table_ER = "drop table if exists $TABLE_NAME_NFRECORD;"
        val drop_Table_E = "drop table if exists $TABLE_NAME_NUTRITIONFACTS;"
        db!!.execSQL(drop_Table_ER)
        db!!.execSQL(drop_Table_E)
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase?) {
        super.onConfigure(db)
        db!!.setForeignKeyConstraintsEnabled(true)
    }

    fun isNutritionFactsTableEmpty() : Boolean
    {
        val strsql = "select * from $TABLE_NAME_NUTRITIONFACTS;"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)
        val count = cursor.count

        cursor.close()
        db.close()

        if(count == 0)  return true

        return false
    }

    fun insertNutritionFacts(nutritionFacts: NutritionFacts):Boolean
    {
        val values = ContentValues()
        values.put(FID, nutritionFacts.fid)
        values.put(FNAME, nutritionFacts.fname)
        values.put(CARB, nutritionFacts.carb)
        values.put(PROTEIN, nutritionFacts.protein)
        values.put(FAT, nutritionFacts.fat)
        values.put(GRAM, nutritionFacts.pergram)
        values.put(KCAL, nutritionFacts.kcal)
        val db = writableDatabase
        val flag = db.insert(TABLE_NAME_NUTRITIONFACTS, null, values)>0
        db.close()
        return flag
    }


    fun findNutritionFactsbyName(fname : String) : ArrayList<NutritionFacts>
    {
        val strsql = "select * from $TABLE_NAME_NUTRITIONFACTS where $FNAME like '%$fname%'"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)
        var result = ArrayList<NutritionFacts>()

        if(cursor.count != 0)
        {
            cursor.moveToFirst()

            //리스트에 추가하기
            do{
                val fid = cursor.getInt(0)
                val fname = cursor.getString(1)
                val carb = cursor.getDouble(2)
                val protein = cursor.getDouble(3)
                val fat = cursor.getDouble(4)
                val pergram = cursor.getDouble(5)
                val kcal = cursor.getDouble(6)

                result.add(NutritionFacts(fid, fname, carb, protein, fat, pergram, kcal))
            }while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return result
    }

    fun findNutritionFacts(fid : Int) : NutritionFacts?
    {
        val strsql = "select * from $TABLE_NAME_NUTRITIONFACTS where $FID='$fid'"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)
        var result : NutritionFacts? = null

        if(cursor.count != 0)
        {
            cursor.moveToFirst()

            val fid = cursor.getInt(0)
            val fname = cursor.getString(1)
            val carb = cursor.getDouble(2)
            val protein = cursor.getDouble(3)
            val fat = cursor.getDouble(4)
            val pergram = cursor.getDouble(5)
            val kcal = cursor.getDouble(6)

            result = NutritionFacts(fid, fname, carb, protein, fat, pergram, kcal)
        }

        cursor.close()
        db.close()

        return result
    }

    fun setNFList(list : ArrayList<NutritionFacts>)
    {
        list.clear()

        val strsql = "select * from $TABLE_NAME_NUTRITIONFACTS;"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)
        if(cursor.count!=0)
        {
            cursor.moveToFirst()
            //레코드 추가하기
            do{
                val fid = cursor.getInt(0)
                val fname = cursor.getString(1)
                val carb = cursor.getDouble(2)
                val protein = cursor.getDouble(3)
                val fat = cursor.getDouble(4)
                val pergram = cursor.getDouble(5)
                val kcal = cursor.getDouble(6)

                list.add(NutritionFacts(fid, fname, carb, protein, fat, pergram, kcal))
                Log.i("NF_db", cursor.getInt(0).toString() + "/" + cursor.getString(1))
            }while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
    }

    fun insertRecord(record: NutritionFactsRecord):Boolean
    {
        val values = ContentValues()
        values.put(RECORDTIME, record.recordtime)
        values.put(FID, record.nutritionFacts.fid)
        values.put(INTAKE, record.intake)
        val db = writableDatabase
        val flag = db.insert(TABLE_NAME_NFRECORD, null, values)>0
        db.close()
        return flag
    }

    fun updateRecord(record: NutritionFactsRecord) : Boolean
    {
        val recordTime = record.recordtime
        val strsql = "select * from $TABLE_NAME_NFRECORD where $RECORDTIME='$recordTime';"
        val db = writableDatabase
        val cursor = db.rawQuery(strsql, null)
        val flag = cursor.count!=0
        if(flag)
        {
            cursor.moveToFirst()
            val values = ContentValues()
            values.put(INTAKE, record.intake)
            db.update(TABLE_NAME_NFRECORD, values, "$RECORDTIME=?", arrayOf(recordTime.toString()))
        }

        cursor.close()
        db.close()
        return flag
    }

    fun deleteRecord(recordTime:String):Boolean
    {
        val strsql = "select * from $TABLE_NAME_NFRECORD where $RECORDTIME='$recordTime'"
        val db = writableDatabase
        val cursor = db.rawQuery(strsql, null)
        val flag = cursor.count!=0
        if(flag)
        {
            cursor.moveToFirst()
            db.delete(TABLE_NAME_NFRECORD, "$RECORDTIME=?", arrayOf(recordTime))
        }

        cursor.close()
        db.close()
        return flag
    }

    fun getRecordList(date: LocalDate) : ArrayList<NutritionFactsRecord>
    {
        val formattedDate = date.toString().replace("-", "")

        val strsql = "select * from $TABLE_NAME_NFRECORD where $RECORDTIME like '$formattedDate%'"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)

        var list = ArrayList<NutritionFactsRecord>()

        if(cursor.count!=0)
        {
            cursor.moveToFirst()

            //리스트에 추가하기
            do{
                val recordtime = cursor.getString(0)
                val nutritionFacts = findNutritionFacts(cursor.getInt(1))
                val intake = cursor.getInt(2)

                list.add(NutritionFactsRecord(recordtime, nutritionFacts!!, intake))
            }while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return list
    }
}
