import dev.xdark.clientapi.event.render.*
import ru.cristalix.clientapi.registerHandler

object IndicatorsManager {

    init {
        registerHandler<HealthRender> { isCancelled = true }
        registerHandler<ExpBarRender>{ isCancelled = true }
        registerHandler<HungerRender>{ isCancelled = true }
        registerHandler<ArmorRender>{ isCancelled = true }
        registerHandler<VehicleHealthRender>{ isCancelled = true }
    }

}