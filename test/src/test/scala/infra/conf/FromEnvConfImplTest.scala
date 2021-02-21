package infra.conf

import domain.conf._
import zio.test.Assertion.equalTo
import zio.test._

object FromEnvConfImplTest extends DefaultRunnableSpec {
  override def spec = suite("FromEnvConfImpl")(testM("get conf from env") {
    for {
      ccAccessKey <- FromEnvConfImpl.ccAccessKey
      ccSecretKey <- FromEnvConfImpl.ccSecretKey
      bfAccessKey <- FromEnvConfImpl.bfAccessKey
      bfSecretKey <- FromEnvConfImpl.bfSecretKey
    } yield assert(ccAccessKey)(
      equalTo(CCEAccessKey.unsafeFrom("ccAccessKey"))
    ) &&
      assert(ccSecretKey)(equalTo(CCESecretKey.unsafeFrom("ccSecretKey"))) &&
      assert(bfAccessKey)(equalTo(BFAccessKey.unsafeFrom("bfAccessKey"))) &&
      assert(bfSecretKey)(equalTo(BFSecretKey.unsafeFrom("bfSecretKey")))
  }).provideCustomLayer(ConfImpl.layer)
}
