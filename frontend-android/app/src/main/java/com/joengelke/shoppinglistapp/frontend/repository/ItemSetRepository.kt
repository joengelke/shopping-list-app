package com.joengelke.shoppinglistapp.frontend.repository

import android.content.Context
import com.joengelke.shoppinglistapp.frontend.common.exception.AppException
import com.joengelke.shoppinglistapp.frontend.models.DeleteResponse
import com.joengelke.shoppinglistapp.frontend.models.ItemSet
import com.joengelke.shoppinglistapp.frontend.network.RetrofitProvider
import com.joengelke.shoppinglistapp.frontend.network.SessionManager
import com.joengelke.shoppinglistapp.frontend.network.TokenManager
import com.joengelke.shoppinglistapp.frontend.utils.JsonHelper.json
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemSetRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val tokenManager: TokenManager,
    private val retrofitProvider: RetrofitProvider
) {

    suspend fun getItemSetsByShoppingList(shoppingListId: String): Result<List<ItemSet>> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response = retrofitProvider.getItemSetApi().getItemSetsByShoppingList("Bearer $token", shoppingListId)
            when {
                response.isSuccessful ->  Result.success(response.body() ?: emptyList())
                response.code() == 401 -> {
                    sessionManager.logout("Unauthorized: try to login again ")
                    Result.failure(Exception("Unauthorized"))
                }

                else -> Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            sessionManager.disconnected("No connection to the Server")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun createItemSet(shoppingListId: String, itemSet: ItemSet, receiptFile: File?): Result<ItemSet> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            // Serialize ItemSet to JSON RequestBody
            val jsonString = json.encodeToString(ItemSet.serializer(), itemSet)
            val itemSetBody = jsonString.toRequestBody("application/json".toMediaType())

            // Prepare optional file part
            val receiptPart = receiptFile?.let { file ->
                val requestBody = file.asRequestBody("application/octet-stream".toMediaType())
                MultipartBody.Part.createFormData("receiptFile", file.name, requestBody)
            }

            val response = retrofitProvider.getItemSetApi().createItemSet("Bearer $token", shoppingListId, itemSetBody, receiptPart)
            when {
                response.isSuccessful -> response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))

                response.code() == 409 -> {
                    Result.failure(AppException.ItemSetNameDuplicationException())
                }

                response.code() == 401 -> {
                    sessionManager.logout("Unauthorized: try to login again ")
                    Result.failure(Exception("Unauthorized"))
                }

                else -> Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            sessionManager.disconnected("No connection to the Server")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun updateItemSet(shoppingListId: String, itemSet: ItemSet, receiptFile: File? = null): Result<ItemSet> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            // filter out itemsetitems with empty name
            val cleanedItemSet = itemSet.copy(
                itemList = itemSet.itemList.filter { it.name.isNotBlank() }
            )

            // Serialize ItemSet to JSON RequestBody
            val jsonString = json.encodeToString(ItemSet.serializer(), cleanedItemSet)
            val itemSetBody = jsonString.toRequestBody("application/json".toMediaType())

            // Prepare optional file part
            val receiptPart = receiptFile?.let { file ->
                val requestBody = file.asRequestBody("application/octet-stream".toMediaType())
                MultipartBody.Part.createFormData("receiptFile", file.name, requestBody)
            }

            val response = retrofitProvider.getItemSetApi().updateItemSet("Bearer $token", shoppingListId, itemSetBody, receiptPart )
            when {
                response.isSuccessful -> response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))
                response.code() == 401 -> {
                    sessionManager.logout("Unauthorized: try to login again ")
                    Result.failure(Exception("Unauthorized"))
                }

                else -> Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            sessionManager.disconnected("No connection to the Server")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun getReceiptFile(itemSetId: String): Result<File> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response = retrofitProvider.getItemSetApi().getReceiptFile("Bearer $token", itemSetId)
            when {
                response.isSuccessful -> {
                    response.body()?.let { body ->
                        val fileName =
                            getFileNameFromHeader(response)
                        val file = File(context.cacheDir, fileName)

                        file.outputStream().use { output ->
                            body.byteStream().copyTo(output)
                        }

                        Result.success(file)
                    } ?: Result.failure(Exception("Empty file body"))
                }

                response.code() == 401 -> {
                    sessionManager.logout("Unauthorized: try to login again")
                    Result.failure(Exception("Unauthorized"))
                }

                else -> Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            sessionManager.disconnected("No connection to the Server")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    private fun getFileNameFromHeader(response: Response<ResponseBody>): String {
        val contentDisposition = response.headers()["Content-Disposition"]
        val regex = Regex("filename=\"(.+?)\"")
        return regex.find(contentDisposition ?: "")?.groups?.get(1)?.value ?: "receipt-file"
    }

    suspend fun deleteItemSet(shoppingListId: String, itemSetId: String): Result<DeleteResponse> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                retrofitProvider.getItemSetApi()
                    .deleteItemSet("Bearer $token", shoppingListId, itemSetId)
            when {
                response.isSuccessful -> response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))
                response.code() == 401 -> {
                    sessionManager.logout("Unauthorized: try to login again ")
                    Result.failure(Exception("Unauthorized"))
                }

                else -> Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            sessionManager.disconnected("No connection to the Server")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

}