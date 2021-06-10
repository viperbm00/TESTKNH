package com.example.knh_prototype0

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.knh_prototype0.databinding.FragmentExerciseBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//운동 정보 기록 프래그먼트
class ExerciseFragment : Fragment() {

    var binding : FragmentExerciseBinding?= null

    //DB에서 가지고 온 운동 정보들.
    val eArrayList = ArrayList<Exercise>()
    //AutoTextComplement에 쓰일 운동 이름 리스트
    val enameArrayList = ArrayList<String>()

    //AutoCompleteTextView의 어댑터
    lateinit var act_adapter : ArrayAdapter<String>
    //운동 기록 RecyclerView의 어댑터
    lateinit var er_adapter : ER_Adapter
    //운동 관련 DBHelper
    lateinit var ERDBHelper: ExerciseDBHelper

    //총 칼로리
    var totalKcal = 0

    //현재 프래그먼트가 보여줄 날짜
    var nowDate : LocalDate = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExerciseBinding.inflate(layoutInflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDB()
        init()
        initRecyclerView(nowDate)

        getjson()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    //공공데이터 인증키
    val key = "8bEhE6amw0GgnRepQJXFDZNHHJR5k4x13kb4Y2C2ogSbIJF74e1BHOSQOTMXxHKvkhzowwE5DYpvRWHWBv2lxQ=="
    val jsonurl = "https://api.odcloud.kr/api/15068730/v1/uddi:12fe14fb-c8ca-47b1-9e53-97a93cb214ed?page=1&perPage=259&serviceKey=" + key
    val scope = CoroutineScope(Dispatchers.IO)

    //오픈api에 접근하여 운동 정보를 받아와 DB에 저장하고 eArrayList와 enameArrayList에 추가해준다.
    fun getjson()
    {
        val isDBEmpty = ERDBHelper.isExerciseTableEmpty()

        scope.launch {

            val doc = Jsoup.connect(jsonurl).ignoreContentType(true).get()

            //json object를 생성
            val json = JSONObject(doc.text())
            val data = json.getJSONArray("data")

            Log.i("exercise", "haha")

            for(i in 0..data.length() -1)
            {
                val exercise = Exercise(i+1, data.getJSONObject(i).getString("운동명"), data.getJSONObject(i).getDouble("MET 계수"))

                if(isDBEmpty)
                {
                    ERDBHelper.insertExercise(exercise)
                    Log.i("exercise", i.toString())
                }

                eArrayList.add(exercise)
                enameArrayList.add(data.getJSONObject(i).getString("운동명"))
            }
        }
    }

    //어댑터와 버튼 이벤트 초기화
    fun init()
    {
        binding?.apply {

            act_adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, enameArrayList)
            enameEditText.setAdapter(act_adapter)

            eRecordBtn.setOnClickListener {
                val ename = enameEditText.text.toString()
                var exercise : Exercise? = null

                for(e in eArrayList)
                {
                    if(e.ename == ename)
                    {
                        exercise = e
                        break;
                    }
                }

                if(exercise != null)
                {
                    val dateTime = LocalDateTime.now().toString().replace("-", "")
                        .replace(":", "")
                        .replace("T", "")

                    val etime = etimeEditText.text.toString().toInt()
                    val weight = weightEditText.text.toString().toInt()
                    val kcal = calcualteKcal(exercise.MET, weight, etime)

                    val record = ExerciseRecord(dateTime, exercise, weight, etime, kcal.toInt())

                    er_adapter.items.add(record)
                    er_adapter.notifyDataSetChanged()

                    val result = ERDBHelper.insertRecord(record)

                    if(result)
                    {
                        Toast.makeText(requireActivity(), "기록 성공!", Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        Toast.makeText(requireActivity(), "기록 실패!", Toast.LENGTH_SHORT).show()
                    }

                    calculateTotalKcal(nowDate)
                }
            }

        }
    }

    fun initDB()
    {
        ERDBHelper = ExerciseDBHelper(requireActivity())

        val dbfile = requireActivity().getDatabasePath("Exercise.db")

        if(!dbfile.parentFile.exists())
        {
            dbfile.parentFile.mkdir()
        }

        /*
        if(!dbfile.exists())
        {
            val file = resources.openRawResource(R.raw)
            val fileSize = file.available()
            val buffer = ByteArray(fileSize)

            file.read(buffer)
            file.close()

            dbfile.createNewFile()
            val output = FileOutputStream(dbfile)
            output.write(buffer)
            output.close()
        }
         */

    }

    //운동 기록 RecyclerView를 초기화 해줌.
    //DB에서 해당 날짜의 기록을 가져온 다음 어댑터를 다시 달아준다.
    fun initRecyclerView(date : LocalDate)
    {
        binding?.apply {
            //adapter for NutritionFacts Records RecyclerView
            eRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            eRecyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
            er_adapter = ER_Adapter(ERDBHelper.getRecordList(date))
            er_adapter.itemClickListener = object : ER_Adapter.OnItemClickListener {
                override fun OnItemClick(
                    holder: ER_Adapter.MyViewHolder,
                    view: View,
                    data: ExerciseRecord,
                    position: Int
                ) {
                    val dlg = ExcersiceRecordEditDialog(requireActivity(), data)
                    dlg.listener = object : ExcersiceRecordEditDialog.OKClickedListener {
                        override fun onOKClicked(eweightText: EditText, etimeText: EditText) {
                            er_adapter.items[position].weight = eweightText.text.toString().toInt()
                            er_adapter.items[position].etime = etimeText.text.toString().toInt()
                            er_adapter.items[position].totalKcal = calcualteKcal(er_adapter.items[position].exercise.MET, er_adapter.items[position].weight, er_adapter.items[position].etime).toInt()
                            ERDBHelper.updateRecord(er_adapter.items[position])
                            er_adapter.notifyItemChanged(position)
                            calculateTotalKcal(nowDate)
                        }
                    }

                    dlg.start()
                }

            }
            eRecyclerView.adapter = er_adapter;
            calculateTotalKcal(nowDate)

            val simpleCallBack = object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN or ItemTouchHelper.UP, ItemTouchHelper.LEFT)
            {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean
                {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
                {
                    ERDBHelper.deleteRecord(er_adapter.items[viewHolder.adapterPosition].recordtime)
                    er_adapter.removeItem(viewHolder.adapterPosition)
                    calculateTotalKcal(nowDate)
                }

                //item move를 막기위해 추가
                override fun isLongPressDragEnabled(): Boolean = false
            }

            //recyclerView에 attach!
            val itemTouchHelper = ItemTouchHelper(simpleCallBack)
            itemTouchHelper.attachToRecyclerView(eRecyclerView)
        }


    }

    //총 소모 칼로리를 계산하고 출력.
    fun calculateTotalKcal(date : LocalDate) {
        var totalKcal = 0

        for (item in er_adapter.items) {
            totalKcal += item.totalKcal
        }

        this.totalKcal = totalKcal

        binding?.etotalKcalText?.text = if (date == LocalDate.now()) {
            "오늘 소모한 칼로리는 " + totalKcal.toString() + " (Kcal) 입니다"
        } else {
            date.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일")) + " 소모한 칼로리는 " + totalKcal.toString() + " (Kcal) 입니다"
        }
    }

    //소모 칼로리 계산
    fun calcualteKcal(met : Double, weight : Int, etime : Int) : Double
    {
        return 0.0175 * met * weight * etime
    }
}