package au.csiro.ict

import org.bson.types.ObjectId
import au.csiro.ict.Validators.Validator
import grizzled.slf4j.Logger
import org.scalatra.ScalatraServlet

trait RestfulHelpers {
  self:ScalatraServlet =>

  object SDBSerializer extends com.codahale.jerkson.Json{
    import org.codehaus.jackson.Version
    import org.codehaus.jackson.map.Module
    import org.codehaus.jackson.map.Module.SetupContext
    import org.codehaus.jackson.`type`.JavaType
    import org.codehaus.jackson.map._
    import annotate.JsonCachable
    import org.codehaus.jackson.JsonGenerator

    class ObjectIdModule extends Module{
      def version = new Version(0, 2, 0, "")
      def getModuleName = "sensordb"
      def setupModule(context: SetupContext) {
        context.addSerializers(new ObjectIdSerializer)
      }
    }
    class ObjectIdSerializer extends Serializers.Base {
      override def findSerializer(config: SerializationConfig, javaType: JavaType, beanDesc: BeanDescription, beanProp: BeanProperty) = {
        val ser: Object = if (classOf[ObjectId].isAssignableFrom(beanDesc.getBeanClass)) { new ObjectIdSerlization } else null
        ser.asInstanceOf[JsonSerializer[Object]]
      }
      @JsonCachable
      class ObjectIdSerlization extends JsonSerializer[ObjectId] {
        def serialize(value: ObjectId, json: JsonGenerator, provider: SerializerProvider) {
          json.writeString(value.toString)
        }
      }
    }
    mapper.registerModule(new ObjectIdModule())
  }

  import SDBSerializer.generate

  implicit def errors:Validator = request.getAttribute("__errors") match {
    case v:Validator =>v
    case others =>
      val validator = new Validator()
      request.setAttribute("__errors",validator)
      validator
  }

  def haltMsg(newErrorMessage:String=null) = {
    if(newErrorMessage!=null)
      errors.addError(newErrorMessage)
    if (!errors.errors.isEmpty)
      halt(400,generate(Map("errors"->errors.errors)))
  }

  val logger: Logger = Logger[this.type]

  def forward(path:String="/session")=servletContext.getRequestDispatcher(path).forward(request, response)


}
