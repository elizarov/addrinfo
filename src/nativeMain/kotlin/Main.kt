import kotlinx.cinterop.*
import platform.posix.*

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: addrinfo <node>")
        return
    }
    val node = args[0]
    memScoped {
        val hints: addrinfo = alloc()
        hints.ai_family = AF_INET
        hints.ai_socktype = SOCK_DGRAM
        val res: CPointerVar<addrinfo> = alloc()
        val err = getaddrinfo(node, null, hints.ptr, res.ptr)
        if (err != 0) {
            println("addrinfo error: ${gai_strerror(err)?.toKString()}")
            return
        }
        var cur = res.value
        while (cur != null) with (cur.pointed) {
            val addr = ai_addr!!.reinterpret<sockaddr_in>().pointed.sin_addr.s_addr
            val str = buildString {
                for (i in 0 until 4) {
                    if (i != 0) append('.')
                    append((addr shr (i * 8)).toUByte())
                }
            }
            println(str)
            cur = ai_next
        }
        freeaddrinfo(res.value)
    }
}