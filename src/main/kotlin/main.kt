import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.net.http.HttpResponse
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap


suspend fun main(args: Array<String>) {

    val categoryValues = Category.values()
    var categoriesNumber = Category.values().size

    var transactions = downloadTransactions()
    //var reportes = hashMapOf<String, Pair<Reporte,FloatArray>>()
    var reportes = hashMapOf<String, Reporte>()

    for ( t in transactions){

        var key = "${t.creation_date.year}-${t.creation_date.month.name}"

        var r = reportes.getOrPut( key ) {
            Reporte( t.creation_date.month.name)
        }

        when( t.status ){
            Status.PENDING -> r.pending++
            Status.REJECTED -> r.rejected++
            Status.DONE -> when( t.operation ) {
                Operation.IN -> r.income += t.amount
                Operation.OUT -> r.expenses += t.amount
            }
        }

        if( t.status == Status.DONE && t.operation == Operation.OUT  ){
            val total = r.expensesByCategory.getOrPut( t.category){ 0f }
            r.expensesByCategory[ t.category ] = total + t.amount
        }

    }


    reportes.forEach { (_, v ) ->

        println(v.month + ":")
        println( "\t${v.pending} transacciones pendientes")
        println( "\t${v.rejected} bloqueadas\n")
        println( "\t$%.2f ingresos\n".format(v.income) )
        println( "\t$%.2f gastos\n".format(v.expenses) )

        val top3 = v.expensesByCategory.toList().sortedByDescending { e -> e.second }.take(3)

        for( topCategory in top3) {
            val categoryName = topCategory.first.name.replace("_"," ").toLowerCase().capitalize()
            val percentage = topCategory.second / v.expenses * 100
            println("\t\t%s %%%02.2f".format(categoryName, percentage) )
        }
    }
}

suspend fun downloadTransactions() : List<Transaction> {
    val transactionsUrl = "https://gist.githubusercontent.com/astrocumbia/06ec83050ec79170b10a11d1d4924dfe/raw/ad791cddcff6df2ec424bfa3da7cdb86f266c57e/transactions.json"

    var transactionJson: String = "{}"

    HttpClient(CIO).use { client ->
        transactionJson = client.get<String>(transactionsUrl)
    }

    val result = Json.decodeFromString<List<Transaction>>(transactionJson);

    return result
}

data class Reporte (
    var month: String,
    var pending: Int = 0,
    var rejected: Int = 0,
    var income: Float = 0f,
    var expenses: Float = 0f,
    var expensesByCategory: HashMap<Category, Float> = hashMapOf()
)

@Serializable
data class Transaction (
    val uuid: Int,
    val description: String,
    val category: Category,
    val operation: Operation,
    val amount: Float,
    val status: Status,

    @Serializable(with = LocalDateSerializer::class)
    val creation_date: LocalDate
)

@Serializable(with = OperationSerializer::class)
enum class Operation {
    IN,
    OUT
}

@Serializable(with = StatusSerializer::class)
enum class Status {
    REJECTED,
    PENDING,
    DONE
}

@Serializable(with = CategorySerializer::class)
enum class  Category{
    ALIMENTACION,
    HOGAR,
    ENTRETENIMIENTO,
    SERVICIOS,
    TRANSFERENCIAS,
    RETIROS_EN_CAJERO,
    TRANSPORTE,
    OTROS,
}


object DateSerializer : KSerializer<Date> {
    private val dateFormat = SimpleDateFormat("MM/dd/yyyy")

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Date  = dateFormat.parse( decoder.decodeString() )
    override fun serialize(encoder: Encoder, value: Date) = encoder.encodeString( dateFormat.format( value) )
}

object LocalDateSerializer : KSerializer<LocalDate> {

    private val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): LocalDate  = LocalDate.parse( decoder.decodeString(), formatter )
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString( value.format(formatter) )
}

object OperationSerializer : KSerializer<Operation> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Operation", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = Operation.valueOf( decoder.decodeString().toUpperCase() )
    override fun serialize(encoder: Encoder, value: Operation) = encoder.encodeString( value.name.toLowerCase() )
}

object StatusSerializer : KSerializer<Status> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Status", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = Status.valueOf( decoder.decodeString().toUpperCase() )
    override fun serialize(encoder: Encoder, value: Status) = encoder.encodeString( value.name.toLowerCase() )
}

object CategorySerializer : KSerializer<Category> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Category", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = Category.valueOf( decoder.decodeString().replace(" ","_").toUpperCase() )
    override fun serialize(encoder: Encoder, value: Category) = encoder.encodeString( value.name.replace("_"," ").toLowerCase() )
}


