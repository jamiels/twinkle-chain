package twinkle.agriledger.webserver.servises


import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import twinkle.agriledger.states.GpsProperties



@Service
class WebSocketService {

    @Autowired
    lateinit var messageTemplate: SimpMessagingTemplate


    val logger = LoggerFactory.getLogger(this.javaClass)


    fun newMessage(info: SendingEmailInfo) {
        messageTemplate.convertAndSend("/notify/newEmail", info)
        logger.info("Email info was sent")
    }

    data class SendingEmailInfo(val producerId: Int, val physicalContainerID: String, val gpsProperties: GpsProperties)

}
