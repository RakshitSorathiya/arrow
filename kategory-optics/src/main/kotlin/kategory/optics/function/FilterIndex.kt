package kategory.optics

import kategory.Applicative
import kategory.HK
import kategory.InstanceParametrizedType
import kategory.Traverse
import kategory.Tuple2
import kategory.Typeclass
import kategory.instance
import kategory.typeLiteral
import kategory.traverse


interface FilterIndex<S, I, A> : Typeclass {

    fun filter(p: (I) -> Boolean): Traversal<S, A>

    companion object {

        /**
         * Lift an instance of [FilterIndex] using an [Iso]
         */
        fun <S, A, I, B> fromIso(FI: FilterIndex<A, I, B>, iso: Iso<S, A>): FilterIndex<S, I, B> = object : FilterIndex<S, I, B> {
            override fun filter(p: (I) -> Boolean): Traversal<S, B> =
                    iso compose FI.filter(p)
        }

        /**
         * Create an instance of [FilterIndex] from a [Traverse] and a function `HK<S, A>) -> HK<S, Tuple2<A, Int>>`
         */
        fun <S, A> fromTraverse(zipWithIndex: (HK<S, A>) -> HK<S, Tuple2<A, Int>>, traverse: Traverse<S>): FilterIndex<HK<S, A>, Int, A> = object : FilterIndex<HK<S, A>, Int, A> {
            override fun filter(p: (Int) -> Boolean): Traversal<HK<S, A>, A> = object : Traversal<HK<S, A>, A> {
                override fun <F> modifyF(FA: Applicative<F>, s: HK<S, A>, f: (A) -> HK<F, A>): HK<F, HK<S, A>> =
                        traverse.traverse(zipWithIndex(s), { (a, j) ->
                            if (p(j)) f(a) else FA.pure(a)
                        }, FA)
            }
        }

    }

}

/**
 * Lift an instance of [FilterIndex] using an [Iso]
 */
inline fun <S, reified A, reified I, reified B> FilterIndex.Companion.fromIso(iso: Iso<S, A>, FI: FilterIndex<A, I, B> = filterIndex()): FilterIndex<S, I, B> =
        fromIso(FI, iso)

/**
 * Create an instance of [FilterIndex] from a [Traverse] and a function `HK<S, A>) -> HK<S, Tuple2<A, Int>>`
 */
inline fun <reified S, A> FilterIndex.Companion.fromTraverse(traverse: Traverse<S> = traverse(), noinline zipWithIndex: (HK<S, A>) -> HK<S, Tuple2<A, Int>>): FilterIndex<HK<S, A>, Int, A> =
        fromTraverse(zipWithIndex, traverse)

inline fun <reified S, reified I, reified A> filterIndex(FI: FilterIndex<S, I, A> = filterIndex(), noinline p: (I) -> Boolean): Traversal<S, A> = FI.filter(p)

inline fun <reified S, reified I, reified A> filterIndex(): FilterIndex<S, I, A> =
        instance(InstanceParametrizedType(FilterIndex::class.java, listOf(typeLiteral<S>(), typeLiteral<I>(), typeLiteral<A>())))