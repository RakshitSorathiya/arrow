package kategory

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.forAll
import kategory.laws.FunctorFilterLaws
import kategory.laws.TraverseFilterLaws
import io.kotlintest.matchers.shouldNotBe
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class OptionTTest : UnitSpec() {
    init {

        "instances can be resolved implicitly" {
            functor<OptionTKindPartial<NonEmptyListHK>>() shouldNotBe null
            applicative<OptionTKindPartial<NonEmptyListHK>>() shouldNotBe null
            monad<OptionTKindPartial<NonEmptyListHK>>() shouldNotBe null
            foldable<OptionTKindPartial<NonEmptyListHK>>() shouldNotBe null
            traverse<OptionTKindPartial<ListKWHK>>() shouldNotBe null
            semigroupK<OptionTKindPartial<ListKWHK>>() shouldNotBe null
            monoidK<OptionTKindPartial<ListKWHK>>() shouldNotBe null
            functorFilter<OptionTKindPartial<ListKWHK>>() shouldNotBe null
            traverseFilter<OptionTKindPartial<OptionHK>>() shouldNotBe null
        }

        val OptionTFIdEq = object : Eq<HK<OptionTKindPartial<IdHK>, Int>> {
            override fun eqv(a: HK<OptionTKindPartial<IdHK>, Int>, b: HK<OptionTKindPartial<IdHK>, Int>): Boolean =
                    a.ev().value == b.ev().value
        }

        val OptionTFOptionEq = object : Eq<HK<OptionTKindPartial<OptionHK>, Int>> {
            override fun eqv(a: HK<OptionTKindPartial<OptionHK>, Int>, b: HK<OptionTKindPartial<OptionHK>, Int>): Boolean =
                    a.ev().value == b.ev().value
        }

        val OptionTFOptionNestedEq = object : Eq<HK<OptionTKindPartial<OptionHK>, HK<OptionTKindPartial<OptionHK>, Int>>> {
            override fun eqv(a: HK<OptionTKindPartial<OptionHK>, HK<OptionTKindPartial<OptionHK>, Int>>, b: HK<OptionTKindPartial<OptionHK>, HK<OptionTKindPartial<OptionHK>, Int>>): Boolean =
                    a.ev().value == b.ev().value
        }

        testLaws(MonadLaws.laws(OptionT.monad(NonEmptyList.monad()), Eq.any()))
        testLaws(SemigroupKLaws.laws(
                OptionT.semigroupK(Id.monad()),
                OptionT.applicative(Id.monad()),
                OptionTFIdEq))

        testLaws(MonoidKLaws.laws(
                OptionT.monoidK(Id.monad()),
                OptionT.applicative(Id.monad()),
                OptionTFIdEq))

        testLaws(FunctorFilterLaws.laws(
                OptionT.functorFilter(),
                { OptionT(Id(it.some())) },
                OptionTFIdEq))

        val nelMonad = monad<NonEmptyListHK>()

        testLaws(TraverseFilterLaws.laws(
                OptionT.traverseFilter(),
                OptionT.applicative(Option.monad()),
                { OptionT(Option(it.some())) },
                OptionTFOptionEq,
                OptionTFOptionNestedEq))

        "toLeft for Some should build a correct EitherT" {
            forAll { a: Int, b: String ->
                OptionT.fromOption<NonEmptyListHK, Int>(Option.Some(a)).toLeft({ b }, nelMonad) == EitherT.left<NonEmptyListHK, Int, String>(a, applicative())
            }
        }

        "toLeft for None should build a correct EitherT" {
            forAll { a: Int, b: String ->
                OptionT.fromOption<NonEmptyListHK, Int>(Option.None).toLeft({ b }, nelMonad) == EitherT.right<NonEmptyListHK, Int, String>(b, applicative())
            }
        }

        "toRight for Some should build a correct EitherT" {
            forAll { a: Int, b: String ->
                OptionT.fromOption<NonEmptyListHK, String>(Option.Some(b)).toRight({ a }, nelMonad) == EitherT.right<NonEmptyListHK, Int, String>(b, applicative())
            }
        }

        "toRight for None should build a correct EitherT" {
            forAll { a: Int, b: String ->
                OptionT.fromOption<NonEmptyListHK, String>(Option.None).toRight({ a }, nelMonad) == EitherT.left<NonEmptyListHK, Int, String>(a, applicative())
            }
        }

    }
}
