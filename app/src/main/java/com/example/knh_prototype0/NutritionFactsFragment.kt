package com.example.knh_prototype0

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.knh_prototype0.databinding.FragmentNutritionFactsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NutritionFactsFragment : Fragment()
{
    var binding : FragmentNutritionFactsBinding ?= null

    lateinit var nf_adapter : NF_Adapter
    lateinit var nfr_adapter : NFR_Adapter
    lateinit var act_adapter : ArrayAdapter<String>

    lateinit var NFDBHelper: NutritionFactsDBHelper

    var listItemClickedFlag = false

    var nowDate : LocalDate = LocalDate.now()

    var nfArray = ArrayList<NutritionFacts>()
    var fnameArray = ArrayList<String>()

    var recordedNFList = ArrayList<NutritionFactsRecord>()

    var totalKcal = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNutritionFactsBinding.inflate(layoutInflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDB()
        init()
        initNFRView(nowDate)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    fun initDB()
    {
        NFDBHelper = NutritionFactsDBHelper(requireActivity())

        val dbfile = requireActivity().getDatabasePath("NutritionFacts.db")

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


        NFDBHelper.getAllRecord()
*/
    }

    val key = "ceffcf39a55843089167"
    val scope = CoroutineScope(Dispatchers.IO)

    fun getjson()
    {
        val isDBEmpty = NFDBHelper.isNutritionFactsTableEmpty()

        scope.launch {

            var startInd = 1
            var endInd = 1000
            val slice = 1000
            val loop = 53314 / slice
            for(i in 0..loop)
            {
                //1, 1001, 2001...
                startInd = i*slice + 1
                //1000, 2000, 3000, 4000, 5000,
                endInd = (i+1)*slice

                if(i == loop)
                {
                    endInd = 53314
                }

                val jsonurl = "http://openapi.foodsafetykorea.go.kr/api/" + key + "/I2790/xml/" + startInd + "/" + endInd
                val doc = Jsoup.connect(jsonurl).parser(Parser.xmlParser()).timeout(500000).get()
                val data = doc.select("row")

                for(item in data)
                {
                    val fid = item.select("NUM").text()
                    val fname = item.select("DESC_KOR").text()
                    val carb = item.select("NUTR_CONT2").text()
                    val protein = item.select("NUTR_CONT3").text()
                    val fat = item.select("NUTR_CONT4").text()
                    val pergram = item.select("SERVING_SIZE").text()
                    val kcal = item.select("NUTR_CONT1").text()

                    if(fname.isEmpty() || carb.isEmpty() || protein.isEmpty() || fat.isEmpty() || pergram.isEmpty() || kcal.isEmpty())
                    {
                        Log.i(fid.toString(), "empty!!")
                        continue
                    }

                    val nutritionFacts = NutritionFacts(fid.toInt(), fname, carb.toDouble(), protein.toDouble(), fat.toDouble(), pergram.toDouble(), kcal.toDouble())

                    Log.i(fid.toString(), fname)
                    NFDBHelper.insertNutritionFacts(nutritionFacts)

                    nfArray.add(nutritionFacts)
                    fnameArray.add(fname)
                }

            }
        }
    }

    //식품 검색 리사이클러뷰
    fun initNFAdapter(arrayList : ArrayList<NutritionFacts>)
    {
        binding?.fnameEditText?.text?.clear()

        nf_adapter = NF_Adapter(ArrayList<NutritionFacts>(arrayList))
        nf_adapter.itemClickListener = object : NF_Adapter.OnItemClickListener {
            override fun OnItemClick(fomerHolder:NF_Adapter.MyViewHolder?, holder: NF_Adapter.MyViewHolder, view: View, selectedFid : Int, data: NutritionFacts, position: Int)
            {
                //눌렀을 때 nametext 채워주기
                binding?.fnameEditText?.setText(data.fname.toString())
                listItemClickedFlag = true

                if(selectedFid == data.fid)
                {
                    nf_adapter.selectedFid = -1
                    holder.setNowHolder(false)
                }
                else
                {
                    fomerHolder?.setNowHolder(false)
                    nf_adapter.selectedFid = data.fid
                    holder.setNowHolder(true)
                }
            }
        }

        binding?.NFRecyclerView?.adapter = nf_adapter;
    }

    fun settingFnameArray(nfarray : ArrayList<NutritionFacts>)
    {
        for(food in nfArray)
        {
            fnameArray.clear()
            fnameArray.add(food.fname);
        }
    }

    fun init()
    {
        binding?.apply {

            //adapter for NutritionFacts RecyclerView
            NFRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            NFRecyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
            NFDBHelper.setNFList(nfArray)
            initNFAdapter(nfArray)

            //adapter for NutritionFacts Records RecyclerView
            NFRRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            NFRRecyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
            nfr_adapter = NFR_Adapter(ArrayList<NutritionFactsRecord>(recordedNFList))
            NFRRecyclerView.adapter = nfr_adapter;

            //음식 이름을 기입하는 EditText. 입력값이 바뀔 때마다 값을 포함하는 음식들을 추려서 NutritionFacts RecyclerView에 보여줌.
            fnameEditText.addTextChangedListener {
                if(it!!.isEmpty())
                {
                    NFDBHelper.setNFList(nfArray)
                    settingFnameArray(nfArray)
                    initNFAdapter(nfArray)
                }
            }

            searchBtn.setOnClickListener {
                val fname = fnameEditText.text.toString()
                initNFAdapter(NFDBHelper.findNutritionFactsbyName(fname))
            }

            intakeEditText.addTextChangedListener {
                if(nf_adapter.selectedFid != -1 && it.toString().isNotEmpty())
                {
                    recordBtn.isEnabled = true
                }
                else
                {
                    recordBtn.isEnabled = false
                }
            }

            recordBtn.setOnClickListener {
                val dateTime = LocalDateTime.now().toString().replace("-", "")
                    .replace(":", "")
                    .replace("T", "")
                val intake = intakeEditText.text.toString().toInt()

                val nutritionFacts = NFDBHelper.findNutritionFacts(nf_adapter.selectedFid)

                val record = NutritionFactsRecord(dateTime, nutritionFacts!!, intake)

                val result = NFDBHelper.insertRecord(record)

                if(result)
                {
                    Toast.makeText(requireActivity(), "기록 성공!", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    Toast.makeText(requireActivity(), "기록 실패!", Toast.LENGTH_SHORT).show()
                }

                nfr_adapter.items.add(record)
                nfr_adapter.notifyDataSetChanged()

                //총 칼로리 누적
                calculateTotalKcal(nowDate)
            }
        }

        act_adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, fnameArray)
        binding?.fnameEditText?.setAdapter(act_adapter)
    }

    fun initNFRView(date : LocalDate)
    {
        binding?.apply {
            //adapter for NutritionFacts Records RecyclerView
            NFRRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            NFRRecyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
            nfr_adapter = NFR_Adapter(NFDBHelper.getRecordList(date))

            nfr_adapter.itemClickListener = object : NFR_Adapter.OnItemClickListener {
                override fun OnItemClick(
                    holder: NFR_Adapter.MyViewHolder,
                    view: View,
                    data: NutritionFactsRecord,
                    position: Int
                ) {
                    val dlg = NutritionFactsRecordEditDialog(requireActivity(), data)

                    dlg.listener = object : NutritionFactsRecordEditDialog.OKClickedListener {
                        override fun onOKClicked(intakeText: EditText) {
                            nfr_adapter.items[position].intake = intakeText.text.toString().toInt()

                            NFDBHelper.updateRecord(nfr_adapter.items[position])
                            nfr_adapter.notifyItemChanged(position)
                            calculateTotalKcal(nowDate)
                        }

                    }

                    dlg.start()
                }
            }
            NFRRecyclerView.adapter = nfr_adapter;
            calculateTotalKcal(nowDate)

            val simpleCallBack = object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN or ItemTouchHelper.UP, ItemTouchHelper.LEFT)
            {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean
                {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
                {
                    NFDBHelper.deleteRecord(nfr_adapter.items[viewHolder.adapterPosition].recordtime)
                    nfr_adapter.removeItem(viewHolder.adapterPosition)
                    calculateTotalKcal(nowDate)
                }

                //item move를 막기위해 추가
                override fun isLongPressDragEnabled(): Boolean = false
            }

            //recyclerView에 attach!
            val itemTouchHelper = ItemTouchHelper(simpleCallBack)
            itemTouchHelper.attachToRecyclerView(NFRRecyclerView)
        }



    }

    fun calculateTotalKcal(date : LocalDate) {
        var totalKcal = 0.0

        for (item in nfr_adapter.items) {
            totalKcal += nfr_adapter.calculateKcal(item)
        }

        this.totalKcal = totalKcal

        binding?.nftotalKcalText?.text = if (date == LocalDate.now()) {
            "오늘 섭취한 칼로리는 " + totalKcal.toString() + " (Kcal) 입니다"
        } else {
            date.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일")) + " 섭취한 칼로리는 " + totalKcal.toString() + " (Kcal) 입니다"
        }
    }
}