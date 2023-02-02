import kotlinx.cinterop.*
import platform.posix.*
import kotlin.time.*
import kotlin.time.TimeSource.*

value class Addr(private val addr: UInt) {
    override fun toString() = buildString {
        for (i in 0 until 4) {
            if (i != 0) append('.')
            append((addr shr (i * 8)).toUByte())
        }
    }
}

fun resolveHostAddr(host: String): List<Addr> = buildList {
    memScoped {
        val hints: addrinfo = alloc()
        hints.ai_family = AF_INET
        hints.ai_socktype = SOCK_DGRAM
        val res: CPointerVar<addrinfo> = alloc()
        val err = getaddrinfo(host, null, hints.ptr, res.ptr)
        if (err != 0) {
            println("addrinfo error: ${gai_strerror(err)?.toKString()}")
            return@buildList
        }
        var cur = res.value
        while (cur != null) with (cur.pointed) {
            add(Addr(ai_addr!!.reinterpret<sockaddr_in>().pointed.sin_addr.s_addr))
            cur = ai_next
        }
        freeaddrinfo(res.value)
    }
}

private const val ESC = 27.toChar()

@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: addrinfo <host>")
        return
    }
    val host = args[0]
    var lastLines = 0
    val known = LinkedHashMap<Addr, Monotonic.ValueTimeMark>()
    var count = 0
    while (true) {
        repeat(lastLines) { print("$ESC[A") }
        println("Resolving $host #${++count}")
        val list = resolveHostAddr(host)
        val now = Monotonic.markNow()
        for (addr in list) known[addr] = now
        for ((addr, mark) in known) {
            val seen = if (mark == now) "now" else "${mark.elapsedNow().toString(DurationUnit.SECONDS)} sec ago"
            println("$ESC[2K$addr - $seen")
        }
        lastLines = known.size + 1
        sleep(1)
    }
}