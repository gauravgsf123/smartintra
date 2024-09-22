package com.swayze.smartintra.network


import com.swayze.smartintra.ui.login.LoginResponse
import com.swayze.smartintra.ui.trip_sheet_printing.TripSheetResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Created by Gaurav on 08,Sep,2024
 */
interface Apis {

    /*@GET("setup/mRegister.htm")
    suspend fun registerDevice(@QueryMap loginMap: Map<String, String>): List<RegisterDeviceResponse>

    @GET("emp/mEmpVerifyDetail.htm")
    suspend fun registration(@QueryMap loginMap: Map<String, String>): List<RegistrationResponseModel>

    @GET("emp/mSaveDailyAttend.htm")
    suspend fun employeeVerification(@QueryMap loginMap: Map<String, String>): List<SaveDailyAttendResponseModel>

    @Multipart
    @POST("emp/mSaveDailyAttend.htm")
    suspend fun markAttendance(@Part dataFile: MultipartBody.Part,
                               @Part("CID") CID: RequestBody?,
                               @Part("EMPNO") EMPNO: RequestBody?,
                               @Part("BID") BID: RequestBody?,
                               @Part("IMEINO") IMEINO: RequestBody?,
                               @Part("MOBILENO") MOBILENO: RequestBody?): List<APIResponse>

    @Multipart
    @POST("sale/mSaveSalesAttend.htm")
    suspend fun markSalesAttendance(@Part dataFile: MultipartBody.Part,
                                    @Part("CID") CID: RequestBody?,
                                    @Part("EMPNO") EMPNO: RequestBody?,
                                    @Part("BID") BID: RequestBody?,
                                    @Part("IMEINO") IMEINO: RequestBody?,
                                    @Part("MOBILENO") MOBILENO: RequestBody?): List<APIResponse>

    @Multipart
    @POST("operation/mAcCopyUpload.htm")
    suspend fun uploadAcCopyData(@Part file: MultipartBody.Part,
                                 @Part("CID") cid: RequestBody?,
                                 @Part("EMPNO") empNo: RequestBody?,
                                 @Part("MOBILENO") mobileNo: RequestBody?,
                                 @Part("BID") bid: RequestBody?,
                                 @Part("DOCKETNO") docketNo: RequestBody?,
                                 @Part("IMEINO") imeiNo: RequestBody?): List<APIResponse>

    @Multipart
    @POST("operation/mPodCopyUpload.htm")
    suspend fun uploadPODCopyData(@Part file: MultipartBody.Part,
                                  @Part("CID") cid: RequestBody?,
                                  @Part("EMPNO") empNo: RequestBody?,
                                  @Part("MOBILENO") mobileNo: RequestBody?,
                                  @Part("BID") bid: RequestBody?,
                                  @Part("DOCKETNO") docketNo: RequestBody?,
                                  @Part("DRSDATE") date: RequestBody?,
                                  @Part("IMEINO") imeiNo: RequestBody?,
                                  @Part("STATUSTYPE") statusType: RequestBody?,
                                  @Part("REASONTYPE") reasonType: RequestBody?,
                                  @Part("REASONS") reason: RequestBody?,
                                  @Part("NCNOTENO") cNoteNumber: RequestBody?): List<APIResponse>

    @Multipart
    @POST("operation/mPodCopyUpload.htm")
    suspend fun uploadPODCopyData(@Part file: MultipartBody.Part,
                                  @Part("CID") cid: RequestBody?,
                                  @Part("EMPNO") empNo: RequestBody?,
                                  @Part("MOBILENO") mobileNo: RequestBody?,
                                  @Part("BID") bid: RequestBody?,
                                  @Part("DOCKETNO") docketNo: RequestBody?,
                                  @Part("DRSDATE") date: RequestBody?,
                                  @Part("IMEINO") imeiNo: RequestBody?): List<APIResponse>

    *//*@POST("viewAllLocation")
    suspend fun getAllLocation(@Body body: Map<String, String>): AllLocationListModel*//*

    @GET("operation/mEkartLocation.htm")
    suspend fun getEKartList(): List<EkartResponseModel>

    @FormUrlEncoded
    @POST("operation/mStockCheck.htm")
    suspend fun uploadStockChecking(@Field("DATASTR") DATASTR: String): List<APIResponse>

    @GET("operation/mBranchList.htm")
    suspend fun getBranchList(@QueryMap loginMap: Map<String, String>): List<BranchListResponseModel>

    @FormUrlEncoded
    @POST("operation/mScanLocation.htm")
    suspend fun scanLocation(@Field("CID") CID: String,
                             @Field("BID") BID: String,
                             @Field("EMPNO") EMPNO: String,
                             @Field("BOXNUMBER") BOXNUMBER: String,
                             @Field("BRANCHNAME") BRANCHNAME: String,
                             @Field("LOCATION") LOCATION: String): List<ScanLocationResponseModel>


    @FormUrlEncoded
    @POST("operation/mPalletScan.htm")
    suspend fun stockPallet(@Field("CID") CID: String,
                             @Field("BID") BID: String,
                             @Field("EMPNO") EMPNO: String,
                             @Field("BOXNUMBER") BOXNUMBER: String,
                             @Field("PALLETNO") PALLETNO: String): List<ScanLocationResponseModel>

    @GET("operation/mDocTypeList.htm")
    suspend fun getDocTypeList(@QueryMap loginMap: Map<String, String>): List<DocTypeListResponseModel>


    @POST("operation/mScanDocData.htm")
    suspend fun scanDocData(@QueryMap loginMap: Map<String, String>): List<ScanDocDataResponseModel>


    @POST("operation/mScanDocTotal.htm")
    suspend fun scanDocTotal(@QueryMap loginMap: Map<String, String>): List<ScanDocTotalResponseModel>


    @POST("operation/mVehicleScan.htm")
    suspend fun uploadVehicleScan(@QueryMap loginMap: Map<String, String>) : List<APIResponse>

    @POST("operation/mStickerData.htm")
    suspend fun getStickerDataList(@QueryMap loginMap: Map<String, String>): List<StickerDataResponseModel>

    @POST("operation/mTataSticker.htm")
    suspend fun getTataStickerDataList(@QueryMap loginMap: Map<String, String>): List<TataStickerDB>

    @GET("setup/mApiVersion.htm")
    suspend fun checkAppVersion(@QueryMap cid: Map<String, String>): List<AppVersionResponse>

    @POST("operation/mVehicleData.htm")
    suspend fun getVehicleDataList(@QueryMap loginMap: Map<String, String>): List<VehicleResponseModel>

    @POST("operation/mNewVehicleScan.htm")
    suspend fun uploadNewVehicleScan(@Body request: VehicleLoadRequest): List<ScanDocDataResponseModel>

    @POST("operation/mExtraScan.htm")
    suspend fun sendExtraScan(@QueryMap loginMap: Map<String, String>): List<SendExtraScanResponseModel>

    @POST("operation/mMobileTrack.htm")
    suspend fun mobileTrack(@QueryMap loginMap: Map<String, String>): Response<List<MobileTrackResponseModel>>

    @POST("operation/mCNoteDrs.htm")
    suspend fun getLimitDate(@QueryMap body: Map<String, String>): List<PodDateLimitResponse>

    @POST("operation/pickupRequest.htm")
    suspend fun pickupRequest(@QueryMap body: Map<String, String>): List<PickupResponseModel>

    @POST("operation/mPickTypes.htm")
    suspend fun pickupTypeReason(@QueryMap body: Map<String, String>): List<PickupReasonResponseModel>

    @POST("operation/mSavePickups.htm")
    suspend fun pickupSave(@QueryMap body: Map<String, String>): List<SaveDailyAttendResponseModel>

    @POST("operation/mDelReason.htm")
    suspend fun delayReason(@QueryMap body: Map<String, String>): List<PODDelayReasonResponse>
    @POST("operation/mPincodeData.htm")
    suspend fun getPinCodeFinder(@QueryMap body: Map<String, String>): List<PinCodeFinderResponseModel>

    @POST("operation/mTrackData.htm")
    suspend fun getTrackData(@QueryMap body: Map<String, String>): List<FinderResponseModel>

    @POST("operation/pickupExists.htm")
    suspend fun getPickupExists(@QueryMap body: Map<String, String>): List<PickupExistsResponseModel>

    @POST("operation/boxPacking.htm")
    suspend fun boxPacking(@Body boxPackingRequestModel: BoxPackingRequestModel): List<BoxPackingResponseModel>

    @POST("operation/mSaveBoxScan.htm")
    suspend fun boxScan(@Body boxWiseScanRequestModel: BoxWiseScanRequestModel): List<BoxPackingResponseModel>

    @Multipart
    @POST("operation/mDepsPodUpload.htm")
    suspend fun uploadDepsPodData(@Part dataFile: MultipartBody.Part,
                                 @Part("CID") cid: RequestBody?,
                                 @Part("BID") bid: RequestBody?,
                                 @Part("EMPNO") empNo: RequestBody?,
                                 @Part("DOCKETNO") docketNo: RequestBody?): List<DepsPodUploadResponse>

    @POST("operation/mTrackingData.htm")
    suspend fun getTrackingData(@QueryMap body: Map<String, String>): List<TrackingResponseModel>
    @GET("operation/freightCalc.htm")
    suspend fun getFreightData(@QueryMap body: Map<String, String>): List<FreightResponseModel>*/
    @POST("LoginVerify")
    suspend fun login(@QueryMap body: Map<String, String>) : Response<List<LoginResponse>>

    @POST("getTripWiseSticker")
    suspend fun getTripSheet(@QueryMap body: Map<String, String>) : Response<List<TripSheetResponse>>
}