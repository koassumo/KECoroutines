package com.example.kecoroutines.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MainViewModel : ViewModel() {
// Для создания корутин используются Корутин-билдеры - специальные методы, extensions-методы
// 1) .launch(): Job - ничего не возвращает, обычно для старты корутины не из корутина (т.к.он не suspend)
//          можно ждать с помощью Job.join (join - это suspend)
// 2) .async(): Deferred<T> - возвращает результат, также можно для старта не из корутины
//          можно ждать с помощью Deferred.await (await - это suspend)
// 3) .withContext(): T - для запуска только из suspend,
//          САМ ОЖИДАЕТ результат блока
// Методы могут быть вызвавы на специальном типе, специальном интерфейсе - coroutine scope
// Например, для VM есть viewmodelScope
// * стартует изначально в главном потоке
// * завершает выполнение корутин, когда вызывается метод onCleared

    init {

        // пример 1. создание launch на scope
        viewModelScope.launch {
            //внутри любой асинхронный код
            Log.d("mTAG", "_____________launch start...")
            delay(5000)
            // delay() - это suspend-функция (видно, если войти в описание)
            // suspend - это функция, которая может быть приостановлена, а потом возобновлена
            // вызываются только из других suspend-функций или suspend-блоков (launch, async)
            Log.d("mTAG", "_____________launch finished")
        }

        // пример 2. создание async на scope
        viewModelScope.async {
            Log.d("mTAG", "_____________acync start...")
            delay(2000)
            Log.d("mTAG", "_____________acync finished")
            // для async возможен return
            return@async
        }
        // delay(1000)
        // если попытаться вызвать delay тут, то не скомпилится, т.к. можно только из блока suspend

        // Что такое scope? Объект, который:
        // 1. Задает время жизни корутин
        // 2. Задает ссылку на контекст ($coroutineContext)
        //      - job (обязательный) - контролирует выполнение
        //          для launch(): Job
        //          для async(): Deferred<T> -наследник Job
        //      - coroutineName - название
        //      - coroutineDispatcher - где и как выполняется, т.е. поток
        //      - coroutineExceptionHandler - логика
        // 3. Определяет методы для запуска


        // ИЕРАРХИЯ scope
        // При вызове scope, он наследует методы от базового, но их можно переопределить при вызове,
        // (Job A)scope.launch(Dispatchers.IO) {...} ---> (Job B)
        // (Job B)scope.withContext(Dispatchers.Main) {...} ---> (Job C)

        // ПРИМЕР использования одновременно 3х разных методов для запуска корутин
        // launch и async - не являются suspend, можно вызывать из главного потока, withContext- нет
        viewModelScope.launch {
            delay(1000)

            val result = withContext(Dispatchers.Default) {
                // Dispatchers.Default - для сложных вычислений. Dispatchers.IO - для сети
                // Dispatchers.Default наследуется ниже наследниками:
                val part1 = async {
                    delay(1000)
                    return@async "Part 1 done"
                }
                val part2 = async {
                    delay(1000)
                    return@async "Part 2 done"
                }
                val part3 = async {
                    delay(1000)
                    return@async "Part 3 done"
                }
                val result1 = part1.await()
                val result2 = part2.await()
                val result3 = part3.await()
                return@withContext "$result1\n$result2\n$result3"
            }
            Log.d("mTAG", "_____________$result")
        }


        // корутины эмиттируют одиночные данные, flow - потоки
        // пример coroutine - собирает все значения вместе, потом разом выдает
        viewModelScope.launch {
            Log.d("mTAG", "_____________пример coroutine")
            anyFunctionCoroutine()
        }

        // аналогичный пример на flowcoroutine - собирает все значения вместе, потом разом выдает

        viewModelScope.launch {
            Log.d("mTAG", "_____________пример coroutine")
            anyFunctionCoroutine()
        }

        viewModelScope.launch {
            Log.d("mTAG", "_____________пример coroutine")
            getUsersFlow().collect { user -> println(user) }
        }

    }

    suspend fun anyFunctionCoroutine() = coroutineScope<Unit>{
        launch {
            getUsersCoroutine().forEach { user -> println("_________$user") }
        }
    }

    suspend fun getUsersCoroutine(): List<String> {
        delay(10000)  // имитация продолжительной работы
        return listOf("____________Tom", "_____________Bob", "____________Sam")
    }


    fun getUsersFlow(): Flow<String> = flow {
        val database = listOf("Tom", "Bob", "Sam")  // условная база данных
        var i = 1;
        for (item in database){
            delay(400L) // имитация продолжительной работы
            println("Emit $i item")
            emit(item) // емитируем значение
            i++
        }
    }



}