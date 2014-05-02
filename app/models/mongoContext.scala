import org.joda.time.format.ISODateTimeFormat

import com.novus.salat._
import com.novus.salat.Context
import com.novus.salat.StringTypeHintStrategy
import com.novus.salat.TypeHintFrequency
import com.novus.salat.json.JSONConfig
import com.novus.salat.json.StringDateStrategy
import com.novus.salat.json.StringObjectIdStrategy

import play.api.Play
import play.api.Play.current

/**
 * Custom salat context for mongo
 */
package object mongoContext {
  implicit val context = new Context {
    val name = "global"
    override val defaultEnumStrategy = EnumStrategy.BY_VALUE
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = "_t")
    override val jsonConfig = JSONConfig(
      dateStrategy = StringDateStrategy(dateFormatter = ISODateTimeFormat.dateTime),
      objectIdStrategy = StringObjectIdStrategy)
  }
  context.registerGlobalKeyOverride(remapThis = "id", toThisInstead = "_id")
  context.registerClassLoader(Play.classloader)
  //context
}