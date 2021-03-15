import org.wartremover.contrib.ContribWarts.autoImport.ContribWart
import wartremover.WartRemover.autoImport.{Wart, Warts}

object Wartremover {
  val wartErrors = Warts.allBut(
    Wart.Any,
    Wart.ImplicitConversion,
    Wart.ImplicitParameter,
    Wart.Nothing,
    Wart.Overloading,
    Wart.DefaultArguments,
    Wart.ToString,
    Wart.TraversableOps,
    Wart.PublicInference,
    Wart.Product,
    Wart.Null,
    Wart.Equals,
    Wart.ToString,
    Wart.Recursion,
    Wart.Var
  ) ++ ContribWart.allBut(
    ContribWart.ExposedTuples,
    ContribWart.Apply,
    ContribWart.MissingOverride,
    ContribWart.SymbolicName,
    ContribWart.NoNeedForMonad,
    ContribWart.SomeApply
  )
}
