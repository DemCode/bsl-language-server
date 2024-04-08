# Lines of code after the asynchronous method call (CodeAfterAsyncCall)

<!-- Блоки выше заполняются автоматически, не трогать -->
## Description
<!-- Описание диагностики заполняется вручную. Необходимо понятным языком описать смысл и схему работу -->
When using asynchronous methods, developers may write lines of code follow immediately after calling the asynchronous method. In this case, the specified lines of code are executed immediately, without waiting for the asynchronous method to execute.

For the correct solution, you need to move all the code that must be executed after the asynchronous action is completed into the export method and specify its name in the notification processing that will be called after the asynchronous action completes. 
Or use asynchronous through promises, for example, `Wait AlertAsync(Text);`

## Examples
<!-- В данном разделе приводятся примеры, на которые диагностика срабатывает, а также можно привести пример, как можно исправить ситуацию -->

Incorrect code
```bsl
&AtClient
Procedure Command1(Command)
    AdditionalParameters = New Structure("Result", 10);
    Notify = New NotifyDescription("AfterNumberWereInputted", ThisObject);
    ShowInputNumber(Notify, 1, "Input quantity", AdditionalParameters.Result, 2);

    Message("Input value is " + AdditionalParameters.Result); // wrong because there will always be 10
EndProcedure

&AtClient
Procedure AfterNumberWereInputted(Number, AdditionalParameters) Export
    If Number <> Undefined Then
        AdditionalParameters.Result = Number;
    EndIf;
EndProcedure;
```

Correct code
```bsl
&AtClient
Procedure Command1(Command)
    AdditionalParameters = New Structure("Result", 10);
    Notify = New NotifyDescription("AfterNumberWereInputted", ThisObject);
    ShowInputNumber(Notify, 1, "Input quantity", AdditionalParameters.Result, 2) 
EndProcedure

&AtClient
Procedure AfterNumberWereInputted(Number, AdditionalParameters) Export
    If Number <> Undefined Then
        AdditionalParameters.Result = Number;
        Message("Input value is " + AdditionalParameters.Result);
    EndIf;
EndProcedure;
```

In some cases, executing code immediately after calling an asynchronous method is entirely possible if you do not need to wait for the results of the asynchronous action. For example
```bsl
&AtClient
Procedure Command(Command)
    ShowMessageBox(, "Message", 10);
    Message("code started working after ShowMessageBox");
    // ...
EndProcedure
```

It is also important to consider that an asynchronous method can be called in one of the code branches and you need to analyze the subsequent code until the end of the current procedure\function. Example:
```bsl
&AtClient
Procedure Команда1(Команда)
    AdditionalParameters = New Structure("Result", 10);
    If Condition Then
        Notify = New NotifyDescription("AfterNumberWereInputted", ThisObject);
        ShowInputNumber(Notify, 1, "Input quantity", AdditionalParameters.Result, 2);
    Else
        // some code is here
    EndIf;
    // may be wrong, because next code is called right after async method call

    Message("Input value is " + AdditionalParameters.Result); // wrong because there will always be 10 
EndProcedure
```

## Sources
<!-- Необходимо указывать ссылки на все источники, из которых почерпнута информация для создания диагностики -->
<!-- Примеры источников

* Source: [Standard: Modules (RU)](https://its.1c.ru/db/v8std#content:456:hdoc)
* Useful information: [Refusal to use modal windows (RU)](https://its.1c.ru/db/metod8dev#content:5272:hdoc)
* Источник: [Cognitive complexity, ver. 1.4](https://www.sonarsource.com/docs/CognitiveComplexity.pdf) -->
- [Developers guide: Internal language. Ch. 4: Sync and async methods (RU)](https://its.1c.ru/db/v8319doc#bookmark:dev:TI000001505)
