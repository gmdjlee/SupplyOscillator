package com.stockoscillator.data.repository

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.stockoscillator.data.model.MarketDepositData
import com.stockoscillator.data.model.StockData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * 주식 데이터 저장소
 */
class StockRepository(private val context: Context) {

    private val python: Python by lazy {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
        Python.getInstance()
    }

    /**
     * 종목 검색
     *
     * @return Pair<ticker, name> or null
     */
    suspend fun searchStock(query: String): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val module = python.getModule("stock_analyzer")
            val result = module.callAttr("search_stock_wrapper", query).toString()

            val json = JSONObject(result)

            if (json.has("error")) {
                null
            } else {
                val ticker = json.getString("ticker")
                val name = json.getString("name")
                Pair(ticker, name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 주식 데이터 수집
     *
     * @param ticker 종목 코드
     * @param days 분석 기간
     * @return StockData or null
     */
    suspend fun getStockData(ticker: String, days: Int = 180): StockData? =
        withContext(Dispatchers.IO) {
            try {
                val module = python.getModule("stock_analyzer")
                val result = module.callAttr("get_stock_analysis", ticker, days).toString()

                val json = JSONObject(result)

                if (json.has("error")) {
                    null
                } else {
                    parseStockData(json)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    /**
     * 증시 자금 동향 데이터 수집
     *
     * @param numPages 수집할 페이지 수
     * @return MarketDepositData or null
     */
    suspend fun getMarketDepositData(numPages: Int = 5): MarketDepositData? =
        withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("StockRepository", "증시 자금 동향 수집 시작: $numPages 페이지")

                val module = python.getModule("stock_analyzer")
                val result = module.callAttr("get_market_deposit_data", numPages).toString()

                android.util.Log.d("StockRepository", "Python 응답: ${result.take(200)}")

                val json = JSONObject(result)

                if (json.has("error")) {
                    val errorMsg = json.getString("error")
                    android.util.Log.e("StockRepository", "증시 데이터 오류: $errorMsg")
                    null
                } else {
                    val data = parseMarketDepositData(json)
                    android.util.Log.d("StockRepository", "데이터 파싱 성공: ${data.dates.size}개")
                    data
                }
            } catch (e: Exception) {
                android.util.Log.e("StockRepository", "증시 데이터 수집 실패", e)
                e.printStackTrace()
                null
            }
        }

    /**
     * 최신 증시 자금 동향
     */
    suspend fun getLatestMarketData(): MarketDepositData? =
        withContext(Dispatchers.IO) {
            try {
                val module = python.getModule("stock_analyzer")
                val result = module.callAttr("get_latest_market_data").toString()

                val json = JSONObject(result)

                if (json.has("error")) {
                    null
                } else {
                    parseMarketDepositData(json)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    /**
     * JSON을 StockData로 파싱
     */
    private fun parseStockData(json: JSONObject): StockData {
        val ticker = json.getString("ticker")
        val name = json.getString("name")

        val dates = jsonArrayToStringList(json.getJSONArray("dates"))
        val marketCap = jsonArrayToLongList(json.getJSONArray("market_cap"))
        val foreign5d = jsonArrayToLongList(json.getJSONArray("foreign_5d"))
        val institution5d = jsonArrayToLongList(json.getJSONArray("institution_5d"))

        return StockData(
            ticker = ticker,
            name = name,
            dates = dates,
            marketCap = marketCap,
            foreign5d = foreign5d,
            institution5d = institution5d
        )
    }

    /**
     * JSON을 MarketDepositData로 파싱
     */
    private fun parseMarketDepositData(json: JSONObject): MarketDepositData {
        val dates = jsonArrayToStringList(json.getJSONArray("dates"))
        val depositAmounts = jsonArrayToDoubleList(json.getJSONArray("deposit_amounts"))
        val depositChanges = jsonArrayToDoubleList(json.getJSONArray("deposit_changes"))
        val creditAmounts = jsonArrayToDoubleList(json.getJSONArray("credit_amounts"))
        val creditChanges = jsonArrayToDoubleList(json.getJSONArray("credit_changes"))

        return MarketDepositData(
            dates = dates,
            depositAmounts = depositAmounts,
            depositChanges = depositChanges,
            creditAmounts = creditAmounts,
            creditChanges = creditChanges
        )
    }

    /**
     * JSONArray를 String List로 변환
     */
    private fun jsonArrayToStringList(array: JSONArray): List<String> {
        return (0 until array.length()).map { array.getString(it) }
    }

    /**
     * JSONArray를 Long List로 변환
     */
    private fun jsonArrayToLongList(array: JSONArray): List<Long> {
        return (0 until array.length()).map { array.getLong(it) }
    }

    /**
     * JSONArray를 Double List로 변환
     */
    private fun jsonArrayToDoubleList(array: JSONArray): List<Double> {
        return (0 until array.length()).map { array.getDouble(it) }
    }
}