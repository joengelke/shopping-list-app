package com.joengelke.shoppinglistapp.frontend.repository

import com.joengelke.shoppinglistapp.frontend.models.*
import com.joengelke.shoppinglistapp.frontend.network.RetrofitProvider
import com.joengelke.shoppinglistapp.frontend.network.SessionManager
import com.joengelke.shoppinglistapp.frontend.network.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val sessionManager: SessionManager,
    private val tokenManager: TokenManager,
    private val retrofitProvider: RetrofitProvider
) {
    suspend fun getRecipesByUserId(): Result<List<Recipe>> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response = retrofitProvider.getRecipeApi().getRecipesByUserId("Bearer $token")
            when {
                response.isSuccessful -> Result.success(response.body() ?: emptyList())
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

    suspend fun getAllRecipes(): Result<List<Recipe>> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response = retrofitProvider.getRecipeApi().getAllRecipes("Bearer $token")
            when {
                response.isSuccessful -> Result.success(response.body() ?: emptyList())
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

    suspend fun getAllMarketplaceRecipesByUserId(): Result<List<Recipe>> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                retrofitProvider.getRecipeApi().getAllMarketplaceRecipesByUserId("Bearer $token")
            when {
                response.isSuccessful -> Result.success(response.body() ?: emptyList())
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

    suspend fun createRecipe(recipe: Recipe): Result<Recipe> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                retrofitProvider.getRecipeApi().createRecipe("Bearer $token", recipe)
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

    suspend fun convertItemSetToRecipe(itemSet: ItemSet): Result<Recipe> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                retrofitProvider.getRecipeApi().convertItemSetToRecipe("Bearer $token", itemSet)
            when {
                response.isSuccessful -> response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))

                response.code() == 409 -> {
                    Result.failure(Exception("Recipe already exists"))
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

    suspend fun changeVisibility(recipeId: String, visibility: Visibility): Result<Recipe> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                retrofitProvider.getRecipeApi()
                    .changeVisibility("Bearer $token", recipeId, visibility)

            when {
                response.isSuccessful -> response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))

                response.code() == 403 -> {
                    Result.failure(Exception("No permissions"))
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

    suspend fun updateRecipe(recipe: Recipe): Result<Recipe> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                retrofitProvider.getRecipeApi().updateRecipe("Bearer $token", recipe)

            when {
                response.isSuccessful -> response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))

                response.code() == 403 -> {
                    Result.failure(Exception("No permissions"))
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

    suspend fun addRecipeToUser(recipeId: String, username: String?): Result<List<Recipe>> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response = retrofitProvider.getRecipeApi()
                .addRecipeToUser("Bearer $token", recipeId, username)

            when {
                response.isSuccessful -> response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))

                response.code() == 401 -> {
                    sessionManager.logout("Unauthorized: try to login again ")
                    Result.failure(Exception("Unauthorized"))
                }

                response.code() == 404 -> {
                    // handle UsernameNotFoundException from Server
                    Result.failure(Exception("Username not found"))
                }

                else -> Result.failure(Exception("Error: ${response.code()}"))
            }

        } catch (e: Exception) {
            sessionManager.disconnected("No connection to the Server")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun removeRecipeFromUser(recipeId: String, userId: String?): Result<DeleteResponse> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response = retrofitProvider.getRecipeApi()
                .removeRecipeFromUser("Bearer $token", recipeId, userId)

            when {
                response.isSuccessful -> response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))

                response.code() == 403 -> {
                    Result.failure(Exception("No permissions"))
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

    suspend fun deleteRecipe(recipeId: String): Result<DeleteResponse> {
        return try {
            val token =
                tokenManager.getToken() ?: return Result.failure(Exception("No token found"))

            val response =
                retrofitProvider.getRecipeApi().deleteRecipe("Bearer $token", recipeId)

            when {
                response.isSuccessful -> response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Unexpected empty response"))

                response.code() == 403 -> {
                    Result.failure(Exception("No permissions"))
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

    suspend fun fetchChefkochRecipe(url: String): Result<Recipe> {
        return withContext(Dispatchers.IO) {
            try {
                val doc = Jsoup.connect(url).get()
                val title = doc.selectFirst("article.recipe-header > div > h1")?.text()
                    ?: doc.selectFirst("#__nuxt > main > div.ds-grid.ds-grid--rds > header > h1")
                        ?.text().orEmpty()

                val ingredientsSection = doc.selectFirst("article.recipe-ingredients")
                    ?: doc.selectFirst("#__nuxt > main > div.ds-grid.ds-grid--rds > section.grid--float-left.ds-d-mb-xs.ds-col-12.ds-col-m-8.or-1")
                val ingredientTables = ingredientsSection?.select("table") ?: emptyList()

                val itemSetItems = ingredientTables.flatMap { table ->
                    // Select only tbody rows, skip thead (headers)
                    table.select("tbody tr").mapNotNull { row ->

                        // Try multiple selectors for amount and unit, fallback to empty string
                        val amountAndUnitRaw = row.selectFirst("td.td-left")?.text()?.trim()
                            ?: row.selectFirst("td.ds-ingredients-table__td--first div")?.text()
                                ?.trim()
                            ?: ""

                        // For name, try multiple selectors including optional properties
                        val nameRaw = row.selectFirst("td.td-right")?.text()?.trim()
                            ?: run {
                                val name = row.selectFirst(".ds-ingredients-table__ingredient-name")
                                    ?.text()?.trim().orEmpty()
                                val properties =
                                    row.selectFirst(".ds-ingredients-table__ingredient-properties")
                                        ?.text()?.trim()
                                if (!properties.isNullOrEmpty()) "$name ($properties)" else name
                            }

                        // Skip empty rows
                        if (amountAndUnitRaw.isEmpty() && nameRaw.isEmpty()) return@mapNotNull null

                        val (amount, unit) = parseAmountAndUnit(amountAndUnitRaw)

                        ItemSetItem(
                            id = "",
                            tmpId = "",
                            name = nameRaw,
                            amount = amount,
                            unit = unit
                        )
                    }
                }

                val portionsInput =
                    doc.selectFirst("article.recipe-ingredients input[name=portionen][type=number]")
                        ?: doc.selectFirst("input.ds-quantity-control__input[name=quantity][type=number]")
                val portions =
                    portionsInput?.attr("value")?.trim()?.takeIf { it.isNotEmpty() } ?: "?"

                val description = if(url.contains(".de", ignoreCase = true))"Rezept für $portions Portionen" else "Recipe for $portions portions"

                val instructions =
                    doc.selectFirst("article.ds-box.ds-grid-float.ds-col-12.ds-col-m-8.ds-or-3 > div:nth-child(3)")
                        ?.html()
                        ?.split(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE))
                        ?.map { it.trim() }
                        ?.filter { it.isNotBlank() }
                        ?.map { it.replace(Regex("^\\s*\\d+[.)\\-:]\\s*"), "") } // Remove prefixes like "1. ", "2) ", "3 -", etc.
                        ?: doc.selectFirst("section.or-3")
                            ?.select("h3 + p > span.instruction__text")
                            ?.map { it.text().trim() }
                            ?.filter { it.isNotBlank() }
                            ?.map { it.replace(Regex("^\\s*\\d+[.)\\-:]\\s*"), "") }
                        ?: emptyList()

                createRecipe(
                    Recipe(
                        id = "",
                        name = title,
                        creatorId = "",
                        createdAt = "",
                        itemSet = ItemSet("", title, itemSetItems, ""),
                        description = description,
                        instructions = instructions,
                        categories = emptyList(),
                        visibility = Visibility.PRIVATE,
                        sharedWithUserIds = emptyList(),
                        receiptFileId = ""
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun fetchCookidooRecipe(url: String): Result<Recipe> {
        return withContext(Dispatchers.IO) {
            try {
                val doc = Jsoup.connect(url).get()
                val title = doc.selectFirst("recipe-card h1")?.text()?.trim().orEmpty()

                val ingredientSection = doc.select("#ingredients-section .recipe-content__inner-section")

                val itemSetItems = ingredientSection.flatMap { section ->
                    val ingredients = section.select("recipe-ingredient")
                    ingredients.mapNotNull { ingredient ->
                        val name = ingredient.selectFirst(".recipe-ingredient__name")?.text()?.trim().orEmpty()
                        val description = ingredient.selectFirst(".recipe-ingredient__description")?.text()?.trim()
                        val fullName = buildString {
                            append(name)
                            if (!description.isNullOrBlank()) append(" ($description)")
                        }

                        val amountText = ingredient.selectFirst(".recipe-ingredient__amount")?.text()?.trim().orEmpty()

                        if (fullName.isBlank() && amountText.isBlank()) return@mapNotNull null

                        val (amount, unit) = parseAmountAndUnit(amountText)

                        ItemSetItem(
                            id = "",
                            tmpId = "",
                            name = fullName,
                            amount = amount,
                            unit = unit
                        )
                    }
                }

                val portionsRaw = doc.selectFirst(".recipe-card__cook-param:has(.icon--servings) > span:last-child")
                    ?.text()
                    ?.trim()
                val portions = portionsRaw?.replace("für", "")?.trim()


                val modelElements = doc.select("#tm-versions-modal > button")
                val models = modelElements.map { it.text().trim() }

                val description = buildString {
                    portions?.let {
                        append("Portionen: $it")
                    }
                    if (models.isNotEmpty()) {
                        if (isNotEmpty()) append(" · ")
                        append("Für Thermomix: ${models.joinToString(", ")}")
                    }
                }

                val instructions = doc
                    .select("#preparation-steps-section ol.recipe-content__ordered-list li")
                    .map { it.text().trim() }
                    .filter { it.isNotBlank() }

                createRecipe(
                    Recipe(
                        id = "",
                        name = title,
                        creatorId = "",
                        createdAt = "",
                        itemSet = ItemSet("", title, itemSetItems, ""),
                        description = description,
                        instructions = instructions,
                        categories = emptyList(),
                        visibility = Visibility.PRIVATE,
                        sharedWithUserIds = emptyList(),
                        receiptFileId = ""
                    )
                )

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun parseAmountAndUnit(raw: String): Pair<Double, String> {
        val fractionMap = mapOf(
            "½" to 0.5, "¼" to 0.25, "¾" to 0.75,
            "⅓" to 1.0 / 3, "⅔" to 2.0 / 3,
            "⅛" to 0.125, "⅜" to 0.375,
            "⅝" to 0.625, "⅞" to 0.875
        )

        val clean = raw.trim()

        // Matches: "1 ½ EL", "½ Bund", "1½ TL", "2/3 Tasse", "4"
        val regex = Regex("""^([0-9]+)?\s*([½¼¾⅓⅔⅛⅜⅝⅞]|[0-9]+/[0-9]+)?\s*(.*)$""")
        val match = regex.find(clean) ?: return 0.0 to clean

        val wholePart = match.groups[1]?.value?.toDoubleOrNull() ?: 0.0
        val fractionPartRaw = match.groups[2]?.value ?: ""
        val unit = match.groups[3]?.value?.trim() ?: ""

        val fractionPart = when {
            fractionMap.containsKey(fractionPartRaw) -> fractionMap[fractionPartRaw] ?: 0.0
            fractionPartRaw.contains("/") -> {
                val parts = fractionPartRaw.split("/")
                val numerator = parts.getOrNull(0)?.toDoubleOrNull()
                val denominator = parts.getOrNull(1)?.toDoubleOrNull()
                if (numerator != null && denominator != null && denominator != 0.0) numerator / denominator else 0.0
            }

            else -> 0.0
        }

        return (wholePart + fractionPart) to unit
    }
}