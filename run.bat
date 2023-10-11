@if "%DEBUG%"=="" echo off

call mvn compile
call mvn exec:java