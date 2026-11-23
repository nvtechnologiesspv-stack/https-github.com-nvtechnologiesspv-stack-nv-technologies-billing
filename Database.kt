package com.nvtechnologies.billing.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sku: String = "",
    val sellingPrice: Double,
    val purchasePrice: Double = 0.0,
    val stock: Int = 0,
    val gstPercent: Double = 18.0
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = ""
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerName: String,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
    val gstEnabled: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val paymentMode: String = "Cash",
    val paidAmount: Double = 0.0
)

@Dao
interface ShopDao {
    @Query("SELECT * FROM products ORDER BY name")
    fun products(): Flow<List<Product>>
    @Insert
    suspend fun addProduct(product: Product)
    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)
    @Query("UPDATE products SET stock = stock - :qty WHERE id = :id AND stock >= :qty")
    suspend fun reduceStock(id: Long, qty: Int): Int

    @Query("SELECT * FROM customers ORDER BY name")
    fun customers(): Flow<List<Customer>>
    @Insert
    suspend fun addCustomer(customer: Customer)

    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun invoices(): Flow<List<Invoice>>
    @Insert
    suspend fun addInvoice(invoice: Invoice)
}

@Database(entities = [Product::class, Customer::class, Invoice::class], version = 1, exportSchema = false)
abstract class ShopDatabase : RoomDatabase() {
    abstract fun dao(): ShopDao
    companion object {
        @Volatile private var INSTANCE: ShopDatabase? = null
        fun get(context: Context): ShopDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, ShopDatabase::class.java, "nv_shop.db").build().also { INSTANCE = it }
        }
    }
}
