# LSBench (Linked Stream Benchmark)

Our Linked Stream Benchmark (LSBench)[1] published in an ISWC paper 2012 [2] has been used in several very recent ICDE[3], SIGMOD[4,7,10] SOSP[5], VLDB[8] and EDBT[6] papers related to streaming graphs, so, we have decided to put some resources in developing and maintaining the source code which was orginally hosted at Google code, https://code.google.com/archive/p/lsbench/ . This is the placeholder for the next release. If you need a direct support for LSBench, don't hesitate to contact me or create an issue in this repository.

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

[3]Youhuan Li ; Lei Zou ; M. Tamer Özsu ; Dongyan Zhao. ICDE 2019.  Time Constrained Continuous Subgraph Search over Streaming Graphs. https://lnkd.in/dticbuQ , source code : https://github.com/pkumod/timingsubg

[4] Kyoungmin Kim, In Seo, Wook-Shin Han, Jeong-Hoon Lee, Sungpack Hong, Hassan Chafi, Hyungyu Shin, and Geonhwa Jeong. SIGMOD 2018. TurboFlux: A Fast Continuous Subgraph Matching System for Streaming Graph Data. https://lnkd.in/dRwNjMD

[5] Yunhao Zhang, Rong Chen, and Haibo Chen. 2017. Sub-millisecond Stateful Stream Querying over Fast-evolving Linked Data. In Proceedings of the 26th Symposium on Operating Systems Principles (SOSP '17). https://dl.acm.org/citation.cfm?id=3132777

[6] Sutanay Choudhury, Lawrence Holder, George Chin, Khushbu Agarwal, John Feo. EDBT 2015. A Selectivity based approach to Continuous Pattern Detection in Streaming Graphs. https://lnkd.in/dunV7PK

[7] Anil Pacaci, Angela Bonifati, and M. Tamer Özsu. 2020. Regular Path Query Evaluation on Streaming Graphs. In Proceedings of the 2020 ACM SIGMOD International Conference on Management of Data (SIGMOD '20). Association for Computing Machinery, New York, NY, USA, 1415–1430. DOI:https://doi.org/10.1145/3318464.3389733, source code https://github.com/dsg-uwaterloo/s-graffito

[8] Symmetric Continuous Subgraph Matching with Bidirectional Dynamic Programming
Seunghwan Min, Sung Gwan Park, Kunsoo Park, Dora Giammarresi, Giuseppe F. Italiano, Wook-shin Han
http://www.vldb.org/pvldb/vol14/p1298-han.pdf (VLDB 2021), source code: https://github.com/SNUCSE-CTA/SymBi

[9] Chi Thang Duong, Trung Dung Hoang, Hongzhi Yin, Matthias Weidlich, Quoc Viet Hung Nguyen, and Karl Aberer. 2021. Efficient streaming subgraph isomorphism with graph neural networks. Proc. VLDB Endow. 14, 5 (January 2021), 730–742. DOI:https://doi.org/10.14778/3446095.3446097, source code https://github.com/graphretrieval/ESSIso

[10] Anil Pacaci, M. Tamer Özsu. Experimental Analysis of Streaming Algorithms forGraph Partitioning. SIGMOD 2019. https://cs.uwaterloo.ca/~apacaci/papers/sigmod19_sgp_authorcopy.pdf, source code : https://github.com/dsg-uwaterloo/s-graffito

[11] Youhuan Li; Lei Zou; M. Tamer Ozsu; Dongyan Zhao. Space-Efficient Subgraph Search over Streaming Graph with Timing Order Constraint https://ieeexplore.ieee.org/document/9248627

[12] Anil Pacaci, Angela Bonifati, M. Tamer Özsu. Evaluating Complex Queries on Streaming Graphs, ICDE 2022. https://arxiv.org/abs/2101.12305

[13].Lefteris Zervakis, Vinay Setty, Christos Tryfonopoulos, Katja Hose. EDBT 2020. Efficient Continuous Multi-Query Processing over Graph Streams. https://arxiv.org/abs/1902.05134

[14] Bucchi, Marco ; Grez, Alejandro ; Quintana, Andrés ; Riveros, Cristian ; Vansummeren, StijnCORE: a Complex Event Recognition Engine. https://arxiv.org/abs/2111.04635. source code: https://github.com/CORE-cer/CORE

[15] Qianzhen Zhang, Deke Guo, Xiang Zhao, and Lailong Luo. 2022. Handling RDF Streams: Harmonizing Subgraph Matching, Adaptive Incremental Maintenance, and Matching-free Updates Together. In Proceedings of the 31st ACM International Conference on Information & Knowledge Management (CIKM '22). https://dl.acm.org/doi/pdf/10.1145/3511808.3557342

[16] Shixuan Sun, Xibo Sun, Bingsheng He, and Qiong Luo. 2022. RapidFlow: an efficient approach to continuous subgraph matching. Proc. VLDB Endow. 15, 11 (July 2022), 2415–2427. https://doi.org/10.14778/3551793.3551803 
