package domain.lib

import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import lib.error.ClientDomainError
import zio.random.Random
import zio.test.Assertion._
import zio.test._

object VOFactoryTest extends DefaultRunnableSpec {
  @newtype case class V(v: NonEmptyString)
  object V extends VOFactory[String, NonEmpty] {
    override type VO = V
  }

  val nonEmptyStringGen: Gen[Random with Sized, String] = for {
    s <- Gen.anyString
    c <- Gen.anyChar
  } yield c.toString + s

  val applyTest = suite("apply")(
    testM("invalid value fails with Throwable")(
      assertM(V("").run)(fails(isSubtype[ClientDomainError](anything)))
    ),
    testM("valid value creates a object") {
      checkM(nonEmptyStringGen) { s =>
        assertM(V(s))(equalTo(V(NonEmptyString.unsafeFrom(s))))
      }
    }
  )

  val applySTest = suite("applyS")(
    testM("invalid value fails with String")(
      assertM(V.applyS("").run)(fails(isSubtype[String](anything)))
    ),
    testM("valid value creates a object") {
      checkM(nonEmptyStringGen) { s =>
        assertM(V.applyS(s))(equalTo(V(NonEmptyString.unsafeFrom(s))))
      }
    }
  )

  val unsafeFromTest = suite("unsafeFrom")(
    test("invalid value throws Exception") {
      assert(V.unsafeFrom(""))(throwsA[IllegalArgumentException])
    },
    testM("valid value creates a object") {
      check(nonEmptyStringGen) { s =>
        assert(V.unsafeFrom(s))(equalTo(V(NonEmptyString.unsafeFrom(s))))
      }
    }
  )

  override def spec = suite("VOFactory")(applyTest, applySTest)
}
