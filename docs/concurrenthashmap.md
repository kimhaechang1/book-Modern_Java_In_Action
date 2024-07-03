## ConcurrentHashMap

`ConcurrentHashMap`은 동시성문제를 해결해주는 `HashMap` 이다.

기존에 `hash` 자료구조를 사용하는데 있어서 동시성을 해결할 수 있는 자료구조는 `HashTable`, `sychronizedMap`이 있엇다.

`HashTable`의 경우에는 대부분의 메소드에 `synchronized` 키워드가 붙어있어서, 여러 스레드가 동시에 해당 메소드에 진입하더라도 동시성을 보장한다.

`synchronizedMap`의 경우 동기화를 위한 락을 객체 자체에 걸어서, 어떤 쓰레드가 메소드를 사용하고 있다면, 다른 메소드를 사용하려고 해도 락을 얻을때 까지 기다려야 한다.

`ConcurrentHashMap`은 `put` 메소드에서 `CAS(Compare and Swap)`을 사용하여 선택적으로 버킷에 lock을 걸고

`get` 메소드 사용에서는 동기화를 보장하지 않는다.

### CAS

https://velog.io/@appti/CASCompare-And-Set

CAS(Compare and Swap)은 동기화 기법 중 하나로

기존의 값과 변경할 값을 전달했을 때, 기존의 값이 메인메모리에서 들고온 값과 일치한다면 변경할 값을 반영한다.

만약 일치하지 않는다면 값을 반영하지않는다.

`ConcurrentHashMap`의 경우 내부적으로 한번도 사용하지 않은 버킷의 key에 대해서

즉, `key`의 해시값에 대한 내부 버킷 인덱스(메인 메모리)의 값이 `null`인지 검사하고,

실제 대입때에도 한번더 `null`인지 검사하여, 이때 여전히 `null`이면 값을 대입하는 방식이다.

여기서 `lock`을 사용하지 않고서도 원자성을 보장받을 수 있는데,

그 이유는 `Unsafe`클래스를 활용하여 하드웨어 수준의 메모리 베리어를 통해 메인 메모리에서 값을 들고오기 때문이다.

### 일반적인 해시충돌이나 동일 key에 대한 대응은?

HashMap과 동일하게 chaining 기법임으로, 동일 key에 대해서는 value를 교체하는 작업을 수행한다.

그게 아니라 key에 hash가 충돌 난 경우에는, 연결리스트의 끝에 value를 추가한다.

이 두가지 작업 모두 버킷에 대해서 락을 건 체로 진입하며,

락을 건 후에도, 혹시나 있을 다른 쓰레드의 변조에 대해서 검사하기 위해 하드웨어 레벨의 검사(`Unsafe 클래스 활용`)를 진행한다.
