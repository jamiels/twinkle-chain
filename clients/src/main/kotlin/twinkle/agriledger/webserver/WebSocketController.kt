package twinkle.agriledger.webserver

import org.springframework.web.bind.annotation.*
import twinkle.agriledger.states.GpsProperties
import twinkle.agriledger.webserver.servises.WebSocketService


@RestController
@RequestMapping("socket")
class WebSocketController(val socketService: WebSocketService){

    @PostMapping("sendEmail")
    fun sendEmail(@RequestParam producerId: Int,
                  @RequestParam physicalContainerID: String,
                  @RequestParam longitude: Float,
                  @RequestParam latitude: Float){
         socketService.newMessage(WebSocketService.SendingEmailInfo(producerId, physicalContainerID, GpsProperties(longitude, latitude)))
    }



}
