import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

const data = [
  { time: "2020/1", liquid: 2000 },
  { time: "2020/2", coincheck: 1500 },
  { time: "2020/3", liquid: 1300 },
  { time: "2020/4", liquid: 2000 },
  { time: "2020/4", coincheck: 1700, liquid: 2000 },
  { time: "2020/6", coincheck: 1500 },
];

const Prices = () => {
  return (
    <LineChart
      width={730}
      height={250}
      data={data}
      margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
    >
      <CartesianGrid strokeDasharray="3 3" />
      <XAxis dataKey="time" />
      <YAxis />
      <Tooltip />
      <Legend />
      <Line type="monotone" dataKey="coincheck" stroke="#8884d8" connectNulls />
      <Line type="monotone" dataKey="liquid" stroke="#82ca9d" connectNulls />
    </LineChart>
  );
};

export default Prices;
