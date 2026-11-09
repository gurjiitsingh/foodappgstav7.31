package com.it10x.foodappgstav7_31.fiskaly

//class FiscalRetryWorker(
//    context: Context,
//    workerParams: WorkerParameters,
//    private val dao: FiscalPendingDao,
//    private val repository: FiskalyRepository
//) : CoroutineWorker(context, workerParams) {
//
//    override suspend fun doWork(): Result {
//
//        val list = dao.getAll()
//
//        list.forEach { item ->
//
//            try {
//
//                val vatList: List<VatAmount> =
//                    Gson().fromJson(item.vatJson, object : TypeToken<List<VatAmount>>() {}.type)
//
//                val paymentList: List<PaymentAmount> =
//                    Gson().fromJson(item.paymentJson, object : TypeToken<List<PaymentAmount>>() {}.type)
//
//                repository.finishTransaction(
//                    item.txId,
//                    item.clientId,
//                    vatList,
//                    paymentList
//                )
//
//                dao.delete(item.id)
//
//            } catch (e: Exception) {
//                // retry later
//            }
//        }
//
//        return Result.success()
//    }
//}





//val workRequest = PeriodicWorkRequestBuilder<FiscalRetryWorker>(
//    15, TimeUnit.MINUTES
//).build()
//
//WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//"fiscal_retry",
//ExistingPeriodicWorkPolicy.KEEP,
//workRequest
//)


//WorkManager.getInstance(context)
//.enqueue(OneTimeWorkRequest.from(FiscalRetryWorker::class.java))