# LSBench (Linked Stream Benchmark)

Our Linked Stream Benchmark (LSBench)[1] published in an ISWC paper 2012 [2] has been used in several very recent ICDE[3], SIGMOD[4,7] SOSP[5] and EDBT[6] papers related to streaming graphs, so, we have decided to put some resources in developing and maintaining the source code. This is the placeholder for the next release. If you need a direct support for LSBench, don't hesitate to contact me or create an issue in this repository.

# The upcoming tasks we are developing:

- [ ] Synchronize with Data Generator of LDBC Social Network Benchmark 
- [ ] Dockerise
- [ ] Support other Formats
- [ ] Support Stream Reasoning Features ( e.g. integrate with LUBM/UOBM ontology generator)
- [ ] Create Adapter to SUMO (traffic simulation engine) to simulate GPS coordinates with Open Street Maps 
- [ ] Build The Test Drivers for Websocket and MQTT


# References: 
[1] Linked Stream Benchmark [code repository on Google Code] (https://lnkd.in/d5reE2h) is now migrated to this repository ([source code](./main/) and [the data generator](./releases/)).

[2] Danh Le-Phuoc, Minh Dao-Tran, Minh-Duc Pham, Peter Boncz, Thomas Eiter and Michael Fink. Linked Stream Data Processing Engines: Facts and Figures.ISWC 2012.  https://lnkd.in/dG3Wp9t

[3]Youhuan Li ; Lei Zou ; M. Tamer Özsu ; Dongyan Zhao. ICDE 2019.  Time Constrained Continuous Subgraph Search over Streaming Graphs. https://lnkd.in/dticbuQ

[4] Kyoungmin Kim, In Seo, Wook-Shin Han, Jeong-Hoon Lee, Sungpack Hong, Hassan Chafi, Hyungyu Shin, and Geonhwa Jeong. SIGMOD 2018. TurboFlux: A Fast Continuous Subgraph Matching System for Streaming Graph Data. https://lnkd.in/dRwNjMD

[5] Yunhao Zhang, Rong Chen, and Haibo Chen. 2017. Sub-millisecond Stateful Stream Querying over Fast-evolving Linked Data. In Proceedings of the 26th Symposium on Operating Systems Principles (SOSP '17). https://dl.acm.org/citation.cfm?id=3132777

[6] Sutanay Choudhury, Lawrence Holder, George Chin, Khushbu Agarwal, John Feo. EDBT 2015. A Selectivity based approach to Continuous Pattern Detection in Streaming Graphs. https://lnkd.in/dunV7PK

[7] Anil Pacaci, Angela Bonifati, and M. Tamer Özsu. 2020. Regular Path Query Evaluation on Streaming Graphs. In Proceedings of the 2020 ACM SIGMOD International Conference on Management of Data (SIGMOD '20). Association for Computing Machinery, New York, NY, USA, 1415–1430. DOI:https://doi.org/10.1145/3318464.3389733
