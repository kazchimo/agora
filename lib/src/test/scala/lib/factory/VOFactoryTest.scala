package lib.factory

import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import zio.ZIO
import zio.test.Assertion._
import zio.test._

object VOFactoryTest extends DefaultRunnableSpec {
  @newtype case class V(v: NonEmptyString)
  object V extends VOFactory[String, NonEmpty] {
    override type VO = V
  }

  val nonEmptyStringGen = for {
    s <- Gen.anyString
    c <- Gen.anyChar
  } yield c.toString + s

  val applyTest = suite("apply")(
    testM("invalid value fails with Throwable")(
      assertM(V("").run)(fails(isSubtype[IllegalArgumentException](anything)))
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

object SumVOFactoryTest extends DefaultRunnableSpec {
  sealed trait Sum { val v: String }
  object Sum    extends SumVOFactory {
    override type VO = Sum
    override val sums: Seq[Sum]               = Seq(A, B)
    override def extractValue(v: Sum): String = v.v
  }
  case object A extends Sum          { val v = "a" }
  case object B extends Sum          { val v = "b" }

  override def spec = suite("SumVOFactory")(applyTest)

  val applyTest = suite("apply")(
    testM("invalid string fails with Throwable") {
      assertM(Sum("c").run)(fails(isSubtype[Throwable](anything)))
    },
    testM("valid string creates an object") {
      ZIO.mapN(assertM(Sum("a"))(equalTo(A)), assertM(Sum("b"))(equalTo(B)))(
        _ && _
      )
    }
  )
}