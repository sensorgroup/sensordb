package au.csiro.ict

import org.bson.types.ObjectId

object JsonGenerator extends com.codahale.jerkson.Json{
  import org.codehaus.jackson.Version
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
