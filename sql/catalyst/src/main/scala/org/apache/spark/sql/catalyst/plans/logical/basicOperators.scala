/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.catalyst.plans.logical

import org.apache.spark.sql.catalyst.expressions._
import org.apache.spark.sql.catalyst.plans._
<<<<<<< HEAD
import org.apache.spark.sql.catalyst.types._

case class Project(projectList: Seq[NamedExpression], child: LogicalPlan) extends UnaryNode {
  def output = projectList.map(_.toAttribute)
=======
import org.apache.spark.sql.types._

case class Project(projectList: Seq[NamedExpression], child: LogicalPlan) extends UnaryNode {
  override def output: Seq[Attribute] = projectList.map(_.toAttribute)

  override lazy val resolved: Boolean = {
    val containsAggregatesOrGenerators = projectList.exists ( _.collect {
        case agg: AggregateExpression => agg
        case generator: Generator => generator
      }.nonEmpty
    )

    !expressions.exists(!_.resolved) && childrenResolved && !containsAggregatesOrGenerators
  }
>>>>>>> githubspark/branch-1.3
}

/**
 * Applies a [[Generator]] to a stream of input rows, combining the
 * output of each into a new stream of rows.  This operation is similar to a `flatMap` in functional
 * programming with one important additional feature, which allows the input rows to be joined with
 * their output.
 * @param join  when true, each output row is implicitly joined with the input tuple that produced
 *              it.
 * @param outer when true, each input row will be output at least once, even if the output of the
 *              given `generator` is empty. `outer` has no effect when `join` is false.
 * @param alias when set, this string is applied to the schema of the output of the transformation
 *              as a qualifier.
 */
case class Generate(
    generator: Generator,
    join: Boolean,
    outer: Boolean,
    alias: Option[String],
    child: LogicalPlan)
  extends UnaryNode {

  protected def generatorOutput: Seq[Attribute] = {
    val output = alias
      .map(a => generator.output.map(_.withQualifiers(a :: Nil)))
      .getOrElse(generator.output)
    if (join && outer) {
      output.map(_.withNullability(true))
    } else {
      output
    }
  }

<<<<<<< HEAD
  override def output =
=======
  override def output: Seq[Attribute] =
>>>>>>> githubspark/branch-1.3
    if (join) child.output ++ generatorOutput else generatorOutput
}

case class Filter(condition: Expression, child: LogicalPlan) extends UnaryNode {
<<<<<<< HEAD
  override def output = child.output
=======
  override def output: Seq[Attribute] = child.output
>>>>>>> githubspark/branch-1.3
}

case class Union(left: LogicalPlan, right: LogicalPlan) extends BinaryNode {
  // TODO: These aren't really the same attributes as nullability etc might change.
<<<<<<< HEAD
  override def output = left.output

  override lazy val resolved =
    childrenResolved &&
    !left.output.zip(right.output).exists { case (l,r) => l.dataType != r.dataType }
=======
  override def output: Seq[Attribute] = left.output

  override lazy val resolved: Boolean =
    childrenResolved &&
    left.output.zip(right.output).forall { case (l,r) => l.dataType == r.dataType }
>>>>>>> githubspark/branch-1.3
}

case class Join(
  left: LogicalPlan,
  right: LogicalPlan,
  joinType: JoinType,
  condition: Option[Expression]) extends BinaryNode {

<<<<<<< HEAD
  override def output = {
=======
  override def output: Seq[Attribute] = {
>>>>>>> githubspark/branch-1.3
    joinType match {
      case LeftSemi =>
        left.output
      case LeftOuter =>
        left.output ++ right.output.map(_.withNullability(true))
      case RightOuter =>
        left.output.map(_.withNullability(true)) ++ right.output
      case FullOuter =>
        left.output.map(_.withNullability(true)) ++ right.output.map(_.withNullability(true))
      case _ =>
        left.output ++ right.output
    }
  }
<<<<<<< HEAD
}

case class Except(left: LogicalPlan, right: LogicalPlan) extends BinaryNode {
  def output = left.output
=======

  private def selfJoinResolved: Boolean = left.outputSet.intersect(right.outputSet).isEmpty

  // Joins are only resolved if they don't introduce ambiguious expression ids.
  override lazy val resolved: Boolean = {
    childrenResolved && !expressions.exists(!_.resolved) && selfJoinResolved
  }
}

case class Except(left: LogicalPlan, right: LogicalPlan) extends BinaryNode {
  override def output: Seq[Attribute] = left.output
>>>>>>> githubspark/branch-1.3
}

case class InsertIntoTable(
    table: LogicalPlan,
    partition: Map[String, Option[String]],
    child: LogicalPlan,
    overwrite: Boolean)
  extends LogicalPlan {

<<<<<<< HEAD
  override def children = child :: Nil
  override def output = child.output

  override lazy val resolved = childrenResolved && child.output.zip(table.output).forall {
    case (childAttr, tableAttr) => childAttr.dataType == tableAttr.dataType
=======
  override def children: Seq[LogicalPlan] = child :: Nil
  override def output: Seq[Attribute] = child.output

  override lazy val resolved: Boolean = childrenResolved && child.output.zip(table.output).forall {
    case (childAttr, tableAttr) =>
      DataType.equalsIgnoreCompatibleNullability(childAttr.dataType, tableAttr.dataType)
>>>>>>> githubspark/branch-1.3
  }
}

case class CreateTableAsSelect[T](
    databaseName: Option[String],
    tableName: String,
    child: LogicalPlan,
    allowExisting: Boolean,
    desc: Option[T] = None) extends UnaryNode {
<<<<<<< HEAD
  override def output = Seq.empty[Attribute]
  override lazy val resolved = databaseName != None && childrenResolved
=======
  override def output: Seq[Attribute] = Seq.empty[Attribute]
  override lazy val resolved: Boolean = databaseName != None && childrenResolved
>>>>>>> githubspark/branch-1.3
}

case class WriteToFile(
    path: String,
    child: LogicalPlan) extends UnaryNode {
<<<<<<< HEAD
  override def output = child.output
}

case class Sort(order: Seq[SortOrder], child: LogicalPlan) extends UnaryNode {
  override def output = child.output
=======
  override def output: Seq[Attribute] = child.output
}

/**
 * @param order  The ordering expressions 
 * @param global True means global sorting apply for entire data set, 
 *               False means sorting only apply within the partition.
 * @param child  Child logical plan              
 */
case class Sort(
    order: Seq[SortOrder],
    global: Boolean,
    child: LogicalPlan) extends UnaryNode {
  override def output: Seq[Attribute] = child.output
>>>>>>> githubspark/branch-1.3
}

case class Aggregate(
    groupingExpressions: Seq[Expression],
    aggregateExpressions: Seq[NamedExpression],
    child: LogicalPlan)
  extends UnaryNode {

<<<<<<< HEAD
  override def output = aggregateExpressions.map(_.toAttribute)
}

case class Limit(limitExpr: Expression, child: LogicalPlan) extends UnaryNode {
  override def output = child.output

  override lazy val statistics: Statistics =
    if (output.forall(_.dataType.isInstanceOf[NativeType])) {
      val limit = limitExpr.eval(null).asInstanceOf[Int]
      val sizeInBytes = (limit: Long) * output.map { a =>
        NativeType.defaultSizeOf(a.dataType.asInstanceOf[NativeType])
      }.sum
      Statistics(sizeInBytes = sizeInBytes)
    } else {
      Statistics(sizeInBytes = children.map(_.statistics).map(_.sizeInBytes).product)
    }
}

case class Subquery(alias: String, child: LogicalPlan) extends UnaryNode {
  override def output = child.output.map(_.withQualifiers(alias :: Nil))
=======
  override def output: Seq[Attribute] = aggregateExpressions.map(_.toAttribute)
}

/**
 * Apply the all of the GroupExpressions to every input row, hence we will get
 * multiple output rows for a input row.
 * @param projections The group of expressions, all of the group expressions should
 *                    output the same schema specified by the parameter `output`
 * @param output      The output Schema
 * @param child       Child operator
 */
case class Expand(
    projections: Seq[GroupExpression],
    output: Seq[Attribute],
    child: LogicalPlan) extends UnaryNode

trait GroupingAnalytics extends UnaryNode {
  self: Product =>
  def gid: AttributeReference
  def groupByExprs: Seq[Expression]
  def aggregations: Seq[NamedExpression]

  override def output: Seq[Attribute] = aggregations.map(_.toAttribute)
}

/**
 * A GROUP BY clause with GROUPING SETS can generate a result set equivalent
 * to generated by a UNION ALL of multiple simple GROUP BY clauses.
 *
 * We will transform GROUPING SETS into logical plan Aggregate(.., Expand) in Analyzer
 * @param bitmasks     A list of bitmasks, each of the bitmask indicates the selected
 *                     GroupBy expressions
 * @param groupByExprs The Group By expressions candidates, take effective only if the
 *                     associated bit in the bitmask set to 1.
 * @param child        Child operator
 * @param aggregations The Aggregation expressions, those non selected group by expressions
 *                     will be considered as constant null if it appears in the expressions
 * @param gid          The attribute represents the virtual column GROUPING__ID, and it's also
 *                     the bitmask indicates the selected GroupBy Expressions for each
 *                     aggregating output row.
 *                     The associated output will be one of the value in `bitmasks`
 */
case class GroupingSets(
    bitmasks: Seq[Int],
    groupByExprs: Seq[Expression],
    child: LogicalPlan,
    aggregations: Seq[NamedExpression],
    gid: AttributeReference = VirtualColumn.newGroupingId) extends GroupingAnalytics

/**
 * Cube is a syntactic sugar for GROUPING SETS, and will be transformed to GroupingSets,
 * and eventually will be transformed to Aggregate(.., Expand) in Analyzer
 *
 * @param groupByExprs The Group By expressions candidates.
 * @param child        Child operator
 * @param aggregations The Aggregation expressions, those non selected group by expressions
 *                     will be considered as constant null if it appears in the expressions
 * @param gid          The attribute represents the virtual column GROUPING__ID, and it's also
 *                     the bitmask indicates the selected GroupBy Expressions for each
 *                     aggregating output row.
 */
case class Cube(
    groupByExprs: Seq[Expression],
    child: LogicalPlan,
    aggregations: Seq[NamedExpression],
    gid: AttributeReference = VirtualColumn.newGroupingId) extends GroupingAnalytics

/**
 * Rollup is a syntactic sugar for GROUPING SETS, and will be transformed to GroupingSets,
 * and eventually will be transformed to Aggregate(.., Expand) in Analyzer
 *
 * @param groupByExprs The Group By expressions candidates, take effective only if the
 *                     associated bit in the bitmask set to 1.
 * @param child        Child operator
 * @param aggregations The Aggregation expressions, those non selected group by expressions
 *                     will be considered as constant null if it appears in the expressions
 * @param gid          The attribute represents the virtual column GROUPING__ID, and it's also
 *                     the bitmask indicates the selected GroupBy Expressions for each
 *                     aggregating output row.
 */
case class Rollup(
    groupByExprs: Seq[Expression],
    child: LogicalPlan,
    aggregations: Seq[NamedExpression],
    gid: AttributeReference = VirtualColumn.newGroupingId) extends GroupingAnalytics

case class Limit(limitExpr: Expression, child: LogicalPlan) extends UnaryNode {
  override def output: Seq[Attribute] = child.output

  override lazy val statistics: Statistics = {
    val limit = limitExpr.eval(null).asInstanceOf[Int]
    val sizeInBytes = (limit: Long) * output.map(a => a.dataType.defaultSize).sum
    Statistics(sizeInBytes = sizeInBytes)
  }
}

case class Subquery(alias: String, child: LogicalPlan) extends UnaryNode {
  override def output: Seq[Attribute] = child.output.map(_.withQualifiers(alias :: Nil))
>>>>>>> githubspark/branch-1.3
}

case class Sample(fraction: Double, withReplacement: Boolean, seed: Long, child: LogicalPlan)
    extends UnaryNode {

<<<<<<< HEAD
  override def output = child.output
}

case class Distinct(child: LogicalPlan) extends UnaryNode {
  override def output = child.output
}

case object NoRelation extends LeafNode {
  override def output = Nil
}

case class Intersect(left: LogicalPlan, right: LogicalPlan) extends BinaryNode {
  override def output = left.output
=======
  override def output: Seq[Attribute] = child.output
}

case class Distinct(child: LogicalPlan) extends UnaryNode {
  override def output: Seq[Attribute] = child.output
}

/**
 * A relation with one row. This is used in "SELECT ..." without a from clause.
 */
case object OneRowRelation extends LeafNode {
  override def output: Seq[Attribute] = Nil

  /**
   * Computes [[Statistics]] for this plan. The default implementation assumes the output
   * cardinality is the product of of all child plan's cardinality, i.e. applies in the case
   * of cartesian joins.
   *
   * [[LeafNode]]s must override this.
   */
  override def statistics: Statistics = Statistics(sizeInBytes = 1)
}

case class Intersect(left: LogicalPlan, right: LogicalPlan) extends BinaryNode {
  override def output: Seq[Attribute] = left.output
>>>>>>> githubspark/branch-1.3
}
